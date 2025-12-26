package com.arplanets.corexrapi.livesight.controller;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.LiveSightErrorCode;
import com.arplanets.corexrapi.livesight.model.dto.PlanDto;
import com.arplanets.corexrapi.livesight.model.dto.req.PlanBatchCreateRequest;
import com.arplanets.corexrapi.livesight.model.dto.req.PlanBatchUpdateRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchCreateResponse;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchUpdateResponse;
import com.arplanets.corexrapi.livesight.service.PlanService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/live-sight/mg/api/plan")
@RequiredArgsConstructor
@Tag(name = "Plan API", description = "Plan API")
@Slf4j
public class PlanController {

    private final PlanService planService;

    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "建立 Plan", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@planPermissionChecker.checkPlanCreatePermission(#request.orgId, #request.liveSightId, #authentication)")
    public ResponseEntity<PlanBatchCreateResponse> create(@RequestBody @Valid PlanBatchCreateRequest request, Authentication authentication) {

        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            username = jwt.getClaimAsString("username");
        }

        if (!StringUtils.hasText(username)) {
            throw new OrderApiException(LiveSightErrorCode._001);
        }

        PlanBatchCreateResponse result = planService.batchCreatePlan(request.getPlans(), request.getLiveSightId(), username);

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/update", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "修改 Plan", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@planPermissionChecker.checkPlanUpdatePermission(#request.orgId, #request.liveSightId, #request.plans, #authentication)")
    public ResponseEntity<PlanBatchUpdateResponse> create(@RequestBody @Valid PlanBatchUpdateRequest request, Authentication authentication) {

        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            username = jwt.getClaimAsString("username");
        }

        if (!StringUtils.hasText(username)) {
            throw new OrderApiException(LiveSightErrorCode._001);
        }

        PlanBatchUpdateResponse result = planService.batchUpdatePlan(request.getPlans(), request.getLiveSightId(), username);

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/debug/{liveSightId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "查詢 Plans (Debug)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, PlanDto>> search(@PathVariable String liveSightId) {
        Map<String, PlanDto> result = planService.findByLiveSightId(liveSightId);

        return ResponseEntity.ok(result);
    }


}
