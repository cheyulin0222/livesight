package com.arplanets.corexrapi.livesight.model.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LiveSightPo {

    private String liveSightId;

    private String orgId;
}
