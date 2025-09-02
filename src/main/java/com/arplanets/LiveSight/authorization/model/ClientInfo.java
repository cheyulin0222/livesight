package com.arplanets.LiveSight.authorization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {

    private String deviceType;

    private String osName;

    private String osVersion;

    private String browserName;

    private String browserVersion;
}
