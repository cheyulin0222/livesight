package com.arplanets.corexrapi.livesight.service.impl;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.LiveSightErrorCode;
import com.arplanets.corexrapi.livesight.mapper.LiveSightMapper;
import com.arplanets.corexrapi.livesight.model.dto.LiveSightDto;
import com.arplanets.corexrapi.livesight.model.po.LiveSightPo;
import com.arplanets.corexrapi.livesight.model.po.ServiceOrgMember;
import com.arplanets.corexrapi.livesight.repository.LiveSightRepository;
import com.arplanets.corexrapi.livesight.repository.ServiceOrgMemberRepository;
import com.arplanets.corexrapi.livesight.service.LiveSightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbLiveSightServiceImpl implements LiveSightService {

    private final ServiceOrgMemberRepository serviceOrgMemberRepository;
    private final LiveSightRepository liveSightRepository;
    private final LiveSightMapper liveSightMapper;


    @Override
    public LiveSightDto createLiveSight(String orgId, String uuid) {

        Optional<ServiceOrgMember> option = serviceOrgMemberRepository.findByOrgIdAndUuid(orgId, uuid);

        if (option.isEmpty()) {
            throw new OrderApiException(LiveSightErrorCode._002);
        }

        String liveSightId = UUID.randomUUID().toString();

        LiveSightPo liveSight = LiveSightPo.builder()
                .liveSightId(liveSightId)
                .orgId(orgId)
                .build();

        liveSightRepository.create(liveSight);

        return liveSightMapper.liveSightPoToLiveSightDto(liveSight);
    }

    @Override
    public LiveSightDto getLiveSight(String liveSightId) {
        LiveSightPo liveSight = findOrThrowByLiveSightId(liveSightId);

        return liveSightMapper.liveSightPoToLiveSightDto(liveSight);
    }

    @Override
    public boolean isLiveSightExist(String liveSightId) {
        Optional<LiveSightPo> option = liveSightRepository.findById(liveSightId);

        return option.isPresent();
    }

    private LiveSightPo findOrThrowByLiveSightId(String liveSightId) {
        Optional<LiveSightPo> option = liveSightRepository.findById(liveSightId);
        if (option.isEmpty()) {
            throw new OrderApiException(LiveSightErrorCode._003);
        }

        return option.get();
    }
}
