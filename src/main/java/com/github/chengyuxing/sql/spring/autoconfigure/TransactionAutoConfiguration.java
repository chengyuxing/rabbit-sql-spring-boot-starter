package com.github.chengyuxing.sql.spring.autoconfigure;

import com.github.chengyuxing.sql.spring.Tx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionAutoConfiguration {
    private final PlatformTransactionManager transactionManager;

    public TransactionAutoConfiguration(@Autowired(required = false) PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean("rabbitTx")
    @ConditionalOnMissingBean(Tx.class)
    public Tx tx() {
        return new Tx(transactionManager);
    }
}
