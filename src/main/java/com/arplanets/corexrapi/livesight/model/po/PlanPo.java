package com.arplanets.corexrapi.livesight.model.po;

import com.arplanets.corexrapi.livesight.model.dto.Expiry;
import lombok.*;

import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanPo {

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
