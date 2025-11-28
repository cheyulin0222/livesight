package com.arplanets.corexrapi.livesight.service.impl;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.LiveSightErrorCode;
import com.arplanets.corexrapi.livesight.mapper.LiveSightMapper;
import com.arplanets.corexrapi.livesight.model.dto.LiveSightDto;
import com.arplanets.corexrapi.livesight.model.po.LiveSightPo;
import com.arplanets.corexrapi.livesight.repository.LiveSightRepository;
import com.arplanets.corexrapi.livesight.service.LiveSightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbLiveSightServiceImpl implements LiveSightService {

    private final LiveSightRepository liveSightRepository;
    private final LiveSightMapper liveSightMapper;
    private static final String LIVE_SIGHT_CACHE = "liveSightDetails";

    @Override
    public LiveSightDto createLiveSight(String orgId, String uuid) {
        String liveSightId = UUID.randomUUID().toString();

        LiveSightPo liveSight = LiveSightPo.builder()
                .liveSightId(liveSightId)
                .orgId(orgId)
                .build();

        liveSightRepository.create(liveSight);

        return liveSightMapper.liveSightPoToLiveSightDto(liveSight);
    }

    @Cacheable(value = LIVE_SIGHT_CACHE, key="#liveSightId")
    @Override
    public LiveSightDto getLiveSight(String liveSightId) {
        LiveSightPo liveSight = findOrThrowByLiveSightId(liveSightId);

        return liveSightMapper.liveSightPoToLiveSightDto(liveSight);
    }

    private LiveSightPo findOrThrowByLiveSightId(String liveSightId) {
        Optional<LiveSightPo> option = liveSightRepository.findById(liveSightId);
        if (option.isEmpty()) {
            throw new OrderApiException(LiveSightErrorCode._003);
        }

        return option.get();
    }
}
