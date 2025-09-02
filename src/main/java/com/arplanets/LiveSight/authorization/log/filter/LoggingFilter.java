package com.arplanets.LiveSight.authorization.log.filter;

import com.arplanets.LiveSight.authorization.log.AuditLogProducer;
import com.arplanets.LiveSight.authorization.log.LogContext;
import com.arplanets.LiveSight.authorization.log.LogMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    private final AuditLogProducer auditLogProducer;
    private final LogContext logContext;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } finally {
            LogMessage logMessage = logContext.buildAuditMessage();

            // 發送日誌到 AuditLogProducer，由它負責批次處理和發送到 SQS
            auditLogProducer.queueLog(logMessage);
        }
    }
}
