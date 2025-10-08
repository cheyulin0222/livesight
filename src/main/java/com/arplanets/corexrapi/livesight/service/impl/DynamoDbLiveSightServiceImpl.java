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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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

    /**
     * 定期清理整個 LiveSight 存在性緩存
     * (需確保 Spring Boot 啟動類有 @EnableScheduling)
     */
    @Scheduled(fixedRate = 3600000) // 1 小時
    @CacheEvict(value = LIVE_SIGHT_CACHE, allEntries = true)
    public void clearLiveSightCache() {
        // 方法被調用時，會自動清空緩存
        // 方法體可以為空
    }


    private LiveSightPo findOrThrowByLiveSightId(String liveSightId) {
        Optional<LiveSightPo> option = liveSightRepository.findById(liveSightId);
        if (option.isEmpty()) {
            throw new OrderApiException(LiveSightErrorCode._003);
        }

        return option.get();
    }
}
