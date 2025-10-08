package com.arplanets.corexrapi.livesight.config;

import com.arplanets.corexrapi.livesight.log.AuditLogProducer;
import com.arplanets.corexrapi.livesight.log.LogContext;
import com.arplanets.corexrapi.livesight.log.filter.InitRequestContextFilter;
import com.arplanets.corexrapi.livesight.log.filter.LoggingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@RequiredArgsConstructor
public class LogConfig {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final LogContext logContext;


    @Bean
    public AuditLogProducer auditLogProducer() {
        return new AuditLogProducer(sqsClient, objectMapper);
    }

    @Bean
    public InitRequestContextFilter initRequestContextFilter() {
        return new InitRequestContextFilter();
    }

    @Bean
    public LoggingFilter loggingFilter() {
        return new LoggingFilter(auditLogProducer(), logContext);
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilterRegistration(LoggingFilter loggingFilter) {
        FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>(loggingFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<InitRequestContextFilter> initRequestContextFilterRegistration(InitRequestContextFilter initRequestContextFilter) {
        FilterRegistrationBean<InitRequestContextFilter> registrationBean = new FilterRegistrationBean<>(initRequestContextFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
