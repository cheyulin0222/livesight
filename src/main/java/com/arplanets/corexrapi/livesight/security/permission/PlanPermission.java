package com.arplanets.corexrapi.livesight.security.permission;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.PermissionDeniedException;
import com.arplanets.corexrapi.livesight.exception.enums.PermissionDeniedErrorCode;
import com.arplanets.corexrapi.livesight.model.dto.LiveSightDto;
import com.arplanets.corexrapi.livesight.service.LiveSightService;
import com.arplanets.corexrapi.livesight.service.OrgService;
import com.arplanets.corexrapi.livesight.service.ServiceOrgMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component("planPermissionChecker")
@RequiredArgsConstructor
@Slf4j
public class PlanPermission {

    private final OrgService orgService;
    private final ServiceOrgMemberService serviceOrgMemberService;
    private final LiveSightService liveSightService;

    public boolean checkPlanCreatePermission(String orgId, String liveSightId, Authentication authentication) {
        // 驗證組織
        validateOrg(orgId);

        // 取得 UUID
        String uuid = extractUuid(authentication);

        // 驗證使用者是否在該組織
        validateMemberInOrg(orgId, uuid);

        // 驗證 Live Sight
        LiveSightDto liveSight = validateLiveSight(liveSightId);

        // 驗證 Live Sight  是否在該組織
        validateLiveSightInOrg(orgId, liveSight);

        return true;
    }

    public boolean checkPlanUpdatePermission(String orgId, String liveSightId, Authentication authentication) {
        // 驗證組織
        validateOrg(orgId);

        // 取得 UUID
        String uuid = extractUuid(authentication);

        // 驗證使用者是否在該組織
        validateMemberInOrg(orgId, uuid);

        // 驗證 Live Sight
        LiveSightDto liveSight = validateLiveSight(liveSightId);

        // 驗證 Live Sight  是否在該組織
        validateLiveSightInOrg(orgId, liveSight);

        return true;
    }

    private void validateOrg(String orgId) {
        try {
            orgService.findByOrgId(orgId);
        } catch (OrderApiException e) {
            throw new PermissionDeniedException(PermissionDeniedErrorCode._001);
        }
    }

    private String extractUuid(Authentication authentication) {
        String uuid = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            uuid = jwt.getClaimAsString("username");
        }

        if (!StringUtils.hasText(uuid)) {
            throw new PermissionDeniedException(PermissionDeniedErrorCode._002);
        }

        return uuid;
    }

    private void validateMemberInOrg(String orgId, String uuid) {
        try {
            serviceOrgMemberService.findByOrgIdAndUuid(orgId, uuid);
        } catch (OrderApiException e) {
            throw new PermissionDeniedException(PermissionDeniedErrorCode._003);
        }
    }

    private LiveSightDto validateLiveSight(String liveSightId) {
        if (!StringUtils.hasText(liveSightId)) {
            throw new PermissionDeniedException(PermissionDeniedErrorCode._004);
        }

        try {
            return liveSightService.getLiveSight(liveSightId);
        } catch (OrderApiException e) {
            throw new PermissionDeniedException(PermissionDeniedErrorCode._008);
        }
    }

    private void validateLiveSightInOrg(String accessOrgId, LiveSightDto liveSight) {
        if (!accessOrgId.equals(liveSight.getOrgId())) {
            throw new PermissionDeniedException(PermissionDeniedErrorCode._005);
        }
    }
}
