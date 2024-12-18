package se.cockroachdb.order.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Indicates the annotated class or method is a non-transactional service boundary. Its architectural role is to
 * delegate to control services or repositories to perform actual business logic processing in
 * the context of an implicit (single-statement, auto-commit) read/write transaction.
 * <p>
 * A meta-annotation including @Transactional, indicating that the annotated class or method
 * must execute non-transactional, hence Propagation.NOT_SUPPORTED.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public @interface TransactionImplicit {
    @AliasFor(annotation = Transactional.class, attribute = "readOnly")
    boolean readOnly() default false;
}
