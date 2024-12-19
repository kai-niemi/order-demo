package se.cockroachdb.order.aspect;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.format.annotation.DurationFormat;
import org.springframework.format.datetime.standard.DurationFormatterUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import se.cockroachdb.order.annotation.TransactionExplicit;
import se.cockroachdb.order.annotation.TransactionImplicit;

@Aspect
@Order(TransactionDelayAspect.PRECEDENCE)
public class TransactionDelayAspect {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The precedence at which this advice is ordered by which also controls
     * the order it is invoked in the call chain between a source and target.
     */
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_ATTRIBUTES_ADVISOR;

    public static void setExplicitTransactionDelay(TransactionDelay delay) {
        currentExplicitTransactionDelay.set(delay);
    }

    public static TransactionDelay getExplicitTransactionDelay() {
        return currentExplicitTransactionDelay.get();
    }

    public static void setImplicitTransactionDelay(TransactionDelay delay) {
        currentImplicitTransactionDelay.set(delay);
    }

    public static TransactionDelay getImplicitTransactionDelay() {
        return currentImplicitTransactionDelay.get();
    }

    public static void clear() {
        currentExplicitTransactionDelay.remove();
        currentImplicitTransactionDelay.remove();
    }

    static <A extends Annotation> A findAnnotation(ProceedingJoinPoint pjp, Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(pjp.getSignature().getDeclaringType(), annotationType);
    }

    private static final ThreadLocal<TransactionDelay> currentExplicitTransactionDelay =
            NamedThreadLocal.withInitial("Current explicit transaction delay",
                    () -> new TransactionDelay(Duration.ofSeconds(5), InjectionPolicy.never));

    private static final ThreadLocal<TransactionDelay> currentImplicitTransactionDelay =
            NamedThreadLocal.withInitial("Current implicit transaction delay",
                    () -> new TransactionDelay(Duration.ofSeconds(5), InjectionPolicy.never));

    private final JdbcTemplate jdbcTemplate;

    public TransactionDelayAspect(DataSource dataSource) {
        Assert.notNull(dataSource, "dataSource is null");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Around(value = "Pointcuts.anyExplicitTransactionBoundaryOperation(transactionExplicit)",
            argNames = "pjp,transactionExplicit")
    public Object doInTransaction(ProceedingJoinPoint pjp, TransactionExplicit transactionExplicit)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting active transaction - check advice @Order and @EnableTransactionManagement order");

        // Grab from type if needed (for non-annotated methods)
        if (transactionExplicit == null) {
            transactionExplicit = findAnnotation(pjp, TransactionExplicit.class);
        }

        Assert.notNull(transactionExplicit, "No @TransactionExplicit annotation found!?");

        return doProceed(pjp, getExplicitTransactionDelay());
    }

    @Around(value = "Pointcuts.anyImplicitTransactionBoundaryOperation(transactionImplicit)",
            argNames = "pjp,transactionImplicit")
    public Object doWithoutTransaction(ProceedingJoinPoint pjp, TransactionImplicit transactionImplicit)
            throws Throwable {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting no active transaction - check advice @Order and @EnableTransactionManagement order");

        // Grab from type if needed (for non-annotated methods)
        if (transactionImplicit == null) {
            transactionImplicit = findAnnotation(pjp, TransactionImplicit.class);
        }

        Assert.notNull(transactionImplicit, "No @TransactionImplicit annotation found!?");

        return doProceed(pjp, getImplicitTransactionDelay());
    }

    private Object doProceed(ProceedingJoinPoint pjp, TransactionDelay transactionDelay) throws Throwable {
        double probability =
                switch (transactionDelay.getInjectionPolicy()) {
                    case never -> 0;
                    case before_25, after_25 -> .25;
                    case before_50, after_50 -> .5;
                    case before_75, after_75 -> .75;
                    case before_always, after_always -> 1;
                };

        boolean beforeOrAfter =
                switch (transactionDelay.getInjectionPolicy()) {
                    case never, after_25, after_50, after_75, after_always -> false;
                    case before_25, before_50, before_75, before_always -> true;
                };

        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (r.nextDouble(0, 1.0) < probability) {
            if (StringUtils.hasLength(transactionDelay.getIdleTimeout())
                && TransactionSynchronizationManager.isActualTransactionActive()) {
                jdbcTemplate.update("SET idle_in_transaction_session_timeout=?", transactionDelay.getIdleTimeout());
            }

            logger.debug("Adding transaction delay %s %s invoking %s".formatted(
                    DurationFormatterUtils.print(transactionDelay.getDelay(), DurationFormat.Style.SIMPLE),
                    beforeOrAfter ? "before" : "after",
                    pjp.getSignature().toShortString()));

            if (beforeOrAfter) {
                try {
                    TimeUnit.MILLISECONDS.sleep(transactionDelay.getDelay().toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                return pjp.proceed();
            } else {
                Object rv = pjp.proceed();

                try {
                    TimeUnit.MILLISECONDS.sleep(transactionDelay.getDelay().toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }

                return rv;
            }
        }

        return pjp.proceed();
    }
}
