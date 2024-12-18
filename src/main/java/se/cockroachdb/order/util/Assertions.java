package se.cockroachdb.order.util;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

public abstract class Assertions {
    private Assertions() {
    }

    public static void assertTransaction() {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "TX not active!");
    }

    public static void assertNoTransaction() {
        Assert.isTrue(!TransactionSynchronizationManager.isActualTransactionActive(), "TX active!");
    }
}
