package se.cockroachdb.order.aspect;

import java.time.Duration;

public class TransactionDelay {
    private final InjectionPolicy injectionPolicy;

    private final Duration delay;

    private String idleTimeout;

    public TransactionDelay(Duration delay, InjectionPolicy injectionPolicy) {
        this.delay = delay;
        this.injectionPolicy = injectionPolicy;
    }

    public Duration getDelay() {
        return delay;
    }

    public InjectionPolicy getInjectionPolicy() {
        return injectionPolicy;
    }

    public String getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @Override
    public String toString() {
        return "TransactionDelay{" +
               "delay=" + delay +
               ", injectionPolicy=" + injectionPolicy +
               ", idleTimeout='" + idleTimeout + '\'' +
               '}';
    }
}
