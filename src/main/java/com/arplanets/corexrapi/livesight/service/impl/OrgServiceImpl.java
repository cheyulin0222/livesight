package com.arplanets.corexrapi.livesight.service.impl;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.OrgErrorCode;
import com.arplanets.corexrapi.livesight.mapper.OrgMapper;
import com.arplanets.corexrapi.livesight.model.dto.OrgDto;
import com.arplanets.corexrapi.livesight.model.po.Org;
import com.arplanets.corexrapi.livesight.repository.OrgRepository;
import com.arplanets.corexrapi.livesight.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrgServiceImpl implements OrgService {

    private final OrgRepository orgRepository;
    private final OrgMapper orgMapper;
    private static final String ORG_CACHE = "orgDetails";

    @Cacheable(value = ORG_CACHE, key = "#orgId")
    @Override
    public OrgDto findByOrgId(String orgId) {
        Optional<Org> option = orgRepository.findByOrgIdAndActiveTrueAndDeletedAtIsNull(orgId);

        if (option.isEmpty()) throw new OrderApiException(OrgErrorCode._001);

        return orgMapper.orgPoToOrgDto(option.get());
    }
}
