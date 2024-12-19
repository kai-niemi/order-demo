package se.cockroachdb.order.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import se.cockroachdb.order.aspect.TransactionDelayAspect;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AspectConfig {
    @Bean
    public TransactionDelayAspect transactionDecoratorAspect(@Autowired DataSource dataSource) {
        return new TransactionDelayAspect(dataSource);
    }
}
