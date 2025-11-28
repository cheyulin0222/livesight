package com.arplanets.corexrapi.livesight.service.impl;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.ServiceOrgMemberErrorCode;
import com.arplanets.corexrapi.livesight.mapper.ServiceOrgMemberMapper;
import com.arplanets.corexrapi.livesight.model.dto.ServiceOrgMemberDto;
import com.arplanets.corexrapi.livesight.model.po.ServiceOrgMember;
import com.arplanets.corexrapi.livesight.repository.ServiceOrgMemberRepository;
import com.arplanets.corexrapi.livesight.service.ServiceOrgMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceOrgMemberServiceImpl implements ServiceOrgMemberService {

    private final ServiceOrgMemberRepository serviceOrgMemberRepository;
    private final ServiceOrgMemberMapper serviceOrgMemberMapper;
    private static final String ORG_MEMBER_CACHE = "orgMemberDetails";

    @Cacheable(value = ORG_MEMBER_CACHE, key = "#orgId + ':' + #uuid")
    @Override
    public ServiceOrgMemberDto findByOrgIdAndUuid(String orgId, String uuid) {
        Optional<ServiceOrgMember> option = serviceOrgMemberRepository.findByOrgIdAndUuidAndActiveTrueAndDeletedAtIsNull(orgId, uuid);

        if (option.isEmpty()) {
            throw new OrderApiException(ServiceOrgMemberErrorCode._001);
        }

        return serviceOrgMemberMapper.serviceOrgMemberPoToServiceOrgMemberDto(option.get());

    }
}
