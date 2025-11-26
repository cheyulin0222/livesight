package com.arplanets.corexrapi.ticket.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.arplanets.corexrapi.ticket.db.arplanetSysLog.repository",
        entityManagerFactoryRef = "sysLogEntityManagerFactory",
        transactionManagerRef = "sysLogTransactionManager"
)
public class SysLogDbConfig {

    @Primary
    @Bean(name = "sysLogDataSource")
    @ConfigurationProperties("spring.datasource.arplanetsyslog")
    public DataSource sysLogDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Primary
    @Bean(name = "sysLogEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sysLogEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sysLogDataSource") DataSource dataSource) {

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");

        return builder
                .dataSource(dataSource)
                .packages("com.arplanets.corexrapi.ticket.db.arplanetSysLog.entity")
                .persistenceUnit("syslog")
                .properties(jpaProperties)
                .build();
    }

    @Primary
    @Bean(name = "sysLogTransactionManager")
    public PlatformTransactionManager sysLogTransactionManager(
            @Qualifier("sysLogEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}