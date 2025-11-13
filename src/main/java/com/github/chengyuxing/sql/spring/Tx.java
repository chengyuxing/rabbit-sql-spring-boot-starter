package com.github.chengyuxing.sql.spring;

import com.github.chengyuxing.sql.exceptions.TransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

public class Tx {
    final PlatformTransactionManager transactionManager;

    public Tx(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public <T> T using(Supplier<T> supplier, TransactionDefinition definition) {
        TransactionStatus status = begin(definition);
        T result;
        try {
            result = supplier.get();
            commit(status);
            return result;
        } catch (Exception e) {
            rollback(status);
            throw new TransactionException("Transaction is rollback.", e);
        }
    }

    public <T> T using(Supplier<T> supplier) {
        return using(supplier, new DefaultTransactionDefinition());
    }

    public void using(Runnable runnable, TransactionDefinition definition) {
        using(() -> {
            runnable.run();
            return 1;
        }, definition);
    }

    public void using(Runnable runnable) {
        using(runnable, new DefaultTransactionDefinition());
    }

    public TransactionStatus begin(TransactionDefinition definition) {
        return transactionManager.getTransaction(definition);
    }

    public TransactionStatus begin() {
        return begin(new DefaultTransactionDefinition());
    }

    public void commit(TransactionStatus status) {
        transactionManager.commit(status);
    }

    public void rollback(TransactionStatus status) {
        transactionManager.rollback(status);
    }
}
