package com.arplanets.corexrapi.livesight.config;

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
        basePackages = "com.arplanets.corexrapi.livesight.repository",
        entityManagerFactoryRef = "orgEntityManagerFactory",
        transactionManagerRef = "orgTransactionManager"
)
public class OrgDbConfig {

    @Bean(name = "orgDataSource")
    @ConfigurationProperties("spring.datasource.arplanetorg")
    public DataSource sysLogDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "orgEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean sysLogEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("orgDataSource") DataSource dataSource) {

        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");

        return builder
                .dataSource(dataSource)
                .packages("com.arplanets.corexrapi.livesight.model.po")
                .persistenceUnit("org")
                .properties(jpaProperties)
                .build();
    }

    @Bean(name = "orgTransactionManager")
    public PlatformTransactionManager sysLogTransactionManager(
            @Qualifier("orgEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
