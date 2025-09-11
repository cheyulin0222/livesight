package com.arplanets.LiveSight.authorization.log.filter;

import com.arplanets.LiveSight.authorization.model.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
public class InitRequestContextFilter extends OncePerRequestFilter {

    private static final String HOURLY_DATE_KEY = "hourlyDate";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    private final String logStreamPrefix;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 設置 MDC ( 讓 CloudWatch 的 LogStream 可以每小時動態更新 )
        setMdc();
        // 設置 Request 資訊 ( 建立 request_id ， 作為 Log 使用 )
        setRequestContext(request);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(HOURLY_DATE_KEY);
        }
    }

    private void setRequestContext(HttpServletRequest request) {
        // 產生 request_id
        String requestId = generateId();

        // 產生 requestContext
        RequestContext requestContext = RequestContext.builder()
                .requestId(requestId)
                .build();

        // 將 requestContext 存到 HttpServletRequest
        request.setAttribute("requestContext", requestContext);
    }

    private void setMdc() {
        String currentHourlyDate = LocalDateTime.now(ZoneId.of("Asia/Taipei")).format(DATE_FORMATTER);
        MDC.put(HOURLY_DATE_KEY, logStreamPrefix + "-logs-" + currentHourlyDate);
    }

    private String generateId() {
        String timestamp = LocalDateTime.now(ZoneId.of("Asia/Taipei"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return "%s-%s-%s".formatted("request", timestamp, UUID.randomUUID().toString());
    }
}
