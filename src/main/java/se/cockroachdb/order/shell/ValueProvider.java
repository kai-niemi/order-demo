package se.cockroachdb.order.shell;

@FunctionalInterface
public interface ValueProvider<T> {
    Object getValue(T object, int column);
}
