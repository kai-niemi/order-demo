package se.cockroachdb.order.aspect;

public enum InjectionPolicy {
    never,

    before_25,
    before_50,
    before_75,
    before_always,

    after_25,
    after_50,
    after_75,
    after_always,
}
