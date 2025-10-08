package com.arplanets.corexrapi.livesight.security;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.PermissionDeniedException;
import com.arplanets.corexrapi.livesight.exception.enums.OrgMemberErrorCode;
import com.arplanets.corexrapi.livesight.service.ServiceOrgMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


@Component("permissionChecker")
@RequiredArgsConstructor
@Slf4j
public class PermissionChecker {

    private final ServiceOrgMemberService serviceOrgMemberService;

    public boolean checkOrgMemberPermission(String orgId, Authentication authentication) {
        String uuid = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            uuid = jwt.getClaimAsString("username");
        }


        if (!StringUtils.hasText(uuid)) {
            throw new PermissionDeniedException(OrgMemberErrorCode._001);
        }

        try {
            serviceOrgMemberService.findByOrgIdAndUuid(orgId, uuid);

        } catch (OrderApiException e) {
            throw new PermissionDeniedException(OrgMemberErrorCode._002);
        }

        return true;
    }
}
