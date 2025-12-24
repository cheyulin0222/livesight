package com.arplanets.corexrapi.livesight.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanDto {

    private String planId;

    private String planName;

    private String liveSightId;

    private Expiry expiry;

    private Boolean standard;

    private ZonedDateTime createdAt;

    private String createdBy;

    private ZonedDateTime updatedAt;

    private String updatedBy;

    private ZonedDateTime deletedAt;
}
