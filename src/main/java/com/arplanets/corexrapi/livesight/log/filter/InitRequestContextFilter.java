package com.arplanets.corexrapi.livesight.log.filter;

import com.arplanets.corexrapi.livesight.model.dto.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RequiredArgsConstructor
public class InitRequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 設置 Request 資訊 ( 建立 request_id ， 作為 Log 使用 )
        setRequestContext(request);

        filterChain.doFilter(request, response);

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



    private String generateId() {
        String timestamp = LocalDateTime.now(ZoneId.of("Asia/Taipei"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        return "%s-%s-%s".formatted("request", timestamp, UUID.randomUUID().toString());
    }
}
