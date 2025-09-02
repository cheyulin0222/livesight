package com.arplanets.LiveSight.authorization.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveSightDto {

    private String liveSightId;
    private String orgId;
}
