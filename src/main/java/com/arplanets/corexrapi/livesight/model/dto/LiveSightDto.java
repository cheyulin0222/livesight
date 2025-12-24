package com.arplanets.corexrapi.livesight.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveSightDto {

    private String liveSightId;
    private String orgId;
    private String createdBy;
    private ZonedDateTime createdAt;
    private String updatedBy;
    private ZonedDateTime updatedAt;
}
