package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.exceptions.TransactionException;
import com.github.chengyuxing.sql.transaction.Definition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

public class SimpleTx {
    final PlatformTransactionManager transactionManager;

    public SimpleTx(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public <T> T using(Supplier<T> supplier, Definition definition) {
        TransactionStatus status = begin(definition);
        T result;
        try {
            result = supplier.get();
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new TransactionException("transaction will rollback cause: ", e);
        }
    }

    public <T> T using(Supplier<T> supplier) {
        return using(supplier, Definition.defaultDefinition());
    }

    public void using(Runnable runnable, Definition definition) {
        using(() -> {
            runnable.run();
            return 1;
        }, definition);
    }

    public void using(Runnable runnable) {
        using(runnable, Definition.defaultDefinition());
    }

    public TransactionStatus begin(Definition definition) {
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setIsolationLevel(definition.getLevel());
        defaultTransactionDefinition.setName(definition.getName());
        defaultTransactionDefinition.setReadOnly(definition.isReadOnly());
        return transactionManager.getTransaction(defaultTransactionDefinition);
    }

    public TransactionStatus begin() {
        return begin(Definition.defaultDefinition());
    }

    public void commit(TransactionStatus status) {
        transactionManager.commit(status);
    }

    public void rollback(TransactionStatus status) {
        transactionManager.rollback(status);
    }
}
