package com.arplanets.spring.security.filter;

import com.arplanets.spring.security.casbin.model.CasbinFactory;
import com.arplanets.spring.security.casbin.model.ResponseCasbinData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class APIExecuteFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

//    @Value("${arp.security.api.user-permission.uri:https://sx8rkasfcb.execute-api.ap-southeast-1.amazonaws.com/prod/permissions/info}")
    private final String apiUserPermissionUri;

    private static final String CASBIN_MODEL_NAME = "execute-api";
    private static final String PERMISSION_ACT = "execute-api";
    private static final String AUTH_DOMAIN = "arplanet";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                throw new AccessDeniedException("Authentication principal is not of type Jwt.");
            }

            String userUuid = jwt.getSubject();
            String accessToken = jwt.getTokenValue();

            ResponseCasbinData cabinResponse = invokeHttpRequestPermissionInfo(accessToken, new String[]{AUTH_DOMAIN});

            String requestURI = request.getRequestURI();

            // 1. 每次請求都創建一個新的 Enforcer 實例
            Enforcer enforcer = CasbinFactory.getEnforcer(CASBIN_MODEL_NAME);

            for(List<String> rule : cabinResponse.getData().getCasbinCsvP()) {
                if ("p".equals(rule.get(0))) {
                    enforcer.addPolicy(rule.subList(1, rule.size()));
                }
            }
            for(List<String> rule : cabinResponse.getData().getCasbinCsvG()) {
                if ("g".equals(rule.get(0))) {
                    enforcer.addGroupingPolicy(rule.subList(1, rule.size()));
                }
            }

            String arn = "arn:arplanet:" + PERMISSION_ACT + ":" + AUTH_DOMAIN + ":" + requestURI;


            boolean isValidate = enforcer.enforce(userUuid, arn, PERMISSION_ACT + ":Invoke", AUTH_DOMAIN);
            if (!isValidate) {
                throw new AccessDeniedException("Execute API permission deny");
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new AccessDeniedException("Permission check failed due to an internal error: " + e.getMessage(), e);
        }

    }

    public ResponseCasbinData invokeHttpRequestPermissionInfo(String bearerToken, String[] domains) {
        ResponseCasbinData res = null;

        try{
            Map<String, Object> payload = new HashMap<>();
            payload.put("doms", domains);
            String jsonPayload = objectMapper.writeValueAsString(payload);
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUserPermissionUri))
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Content-Type", "application/json; utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseContent = response.body();

                res = objectMapper.readValue(responseContent, ResponseCasbinData.class);

                if (!res.getSuccess()) {
                    throw new Exception(res.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return res;
    }
}
