package com.arplanets.corexrapi.livesight.controller;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.LiveSightErrorCode;
import com.arplanets.corexrapi.livesight.model.dto.LiveSightDto;
import com.arplanets.corexrapi.livesight.model.dto.req.LiveSightCreateRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.LiveSightResponse;
import com.arplanets.corexrapi.livesight.service.LiveSightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mg/api/live-sight")
@RequiredArgsConstructor
@Tag(name = "Live Sight API", description = "Live Sight API API")
@Slf4j
public class LiveSightController {

    private final LiveSightService liveSightService;

    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "建立 Live Sight", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@permissionChecker.checkOrgMemberPermission(#request.orgId, #authentication)")
    public ResponseEntity<LiveSightResponse> create(@RequestBody @Valid LiveSightCreateRequest request, Authentication authentication) {

        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            username = jwt.getClaimAsString("username");
        }

        if (!StringUtils.hasText(username)) {
            throw new OrderApiException(LiveSightErrorCode._001);
        }

        LiveSightDto result = liveSightService.createLiveSight(request.getOrgId(), username);

        LiveSightResponse response = LiveSightResponse.builder()
                .serviceTypeId(result.getLiveSightId())
                .build();

        return ResponseEntity.ok(response);


    }
}
