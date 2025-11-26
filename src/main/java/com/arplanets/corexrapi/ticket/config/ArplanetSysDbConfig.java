package com.arplanets.corexrapi.ticket.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.arplanets.corexrapi.ticket.db.arplanetSys.repository",
        entityManagerFactoryRef = "arplanetSysEntityManagerFactory",
        transactionManagerRef = "arplanetSysTransactionManager"
)
public class ArplanetSysDbConfig {

    @Bean(name = "arplanetSysDataSource")
    @ConfigurationProperties("spring.datasource.arplanetsys")
    public DataSource arplanetSysDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "arplanetSysEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean arplanetSysEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("arplanetSysDataSource") DataSource dataSource) {

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");

        return builder
                .dataSource(dataSource)
                .packages("com.arplanets.corexrapi.ticket.db.arplanetSys.entity")
                .persistenceUnit("arplanetsys")
                .properties(jpaProperties)
                .build();
    }

    @Bean(name = "arplanetSysTransactionManager")
    public PlatformTransactionManager arplanetSysTransactionManager(
            @Qualifier("arplanetSysEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}