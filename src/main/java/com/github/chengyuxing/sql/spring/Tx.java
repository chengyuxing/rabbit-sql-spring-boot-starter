package com.github.chengyuxing.sql.spring;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.function.Supplier;

/**
 * A utility class for managing transactions in a Spring environment. It provides methods to begin, commit, and rollback
 * transactions, as well as to execute operations within a transactional context.
 */
public class Tx {
    final PlatformTransactionManager transactionManager;

    public Tx(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Executes the given supplier within a transactional context.
     *
     * @param supplier   the supplier to be executed within the transaction
     * @param definition the transaction definition specifying the characteristics of the transaction
     * @param <T>        the type of the result object returned by the supplier
     * @return the result of the supplier's get method
     */
    public <T> T using(Supplier<T> supplier, TransactionDefinition definition) {
        TransactionStatus status = begin(definition);
        T result;
        try {
            result = supplier.get();
            commit(status);
            return result;
        } catch (Exception e) {
            rollback(status);
            throw e;
        }
    }

    /**
     * Executes the given supplier within a transactional context using a default transaction definition.
     *
     * @param supplier the supplier to be executed within the transaction
     * @param <T>      the type of the result object returned by the supplier
     * @return the result of the supplier's get method
     */
    public <T> T using(Supplier<T> supplier) {
        return using(supplier, new DefaultTransactionDefinition());
    }

    /**
     * Executes the given runnable within a transactional context.
     *
     * @param runnable   the runnable to be executed within the transaction
     * @param definition the transaction definition specifying the characteristics of the transaction
     */
    public void using(Runnable runnable, TransactionDefinition definition) {
        using(() -> {
            runnable.run();
            return 1;
        }, definition);
    }

    /**
     * Executes the given runnable within a transactional context using a default transaction definition.
     *
     * @param runnable the runnable to be executed within the transaction
     */
    public void using(Runnable runnable) {
        using(runnable, new DefaultTransactionDefinition());
    }

    /**
     * Begins a new transaction with the specified definition.
     *
     * @param definition the transaction definition specifying the characteristics of the transaction
     * @return the status of the newly created transaction
     */
    public TransactionStatus begin(TransactionDefinition definition) {
        return transactionManager.getTransaction(definition);
    }

    /**
     * Begins a new transaction using the default transaction definition.
     *
     * @return the status of the newly created transaction
     */
    public TransactionStatus begin() {
        return begin(new DefaultTransactionDefinition());
    }

    /**
     * Commits the current transaction.
     *
     * @param status the TransactionStatus object representing the transaction to be committed
     */
    public void commit(TransactionStatus status) {
        transactionManager.commit(status);
    }

    /**
     * Rolls back the current transaction.
     *
     * @param status the TransactionStatus object representing the transaction to be rolled back
     */
    public void rollback(TransactionStatus status) {
        transactionManager.rollback(status);
    }
}
