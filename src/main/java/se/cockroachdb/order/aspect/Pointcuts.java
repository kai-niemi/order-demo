package se.cockroachdb.order.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import se.cockroachdb.order.annotation.TransactionExplicit;
import se.cockroachdb.order.annotation.TransactionImplicit;

@Aspect
public class Pointcuts {
    @Pointcut("execution(public * *(..)) "
              + "&& @annotation(transactionExplicit)")
    public void anyExplicitTransactionBoundaryOperation(TransactionExplicit transactionExplicit) {
    }

    @Pointcut("execution(public * *(..)) "
              + "&& @annotation(transactionImplicit)")
    public void anyImplicitTransactionBoundaryOperation(TransactionImplicit transactionImplicit) {
    }
}

