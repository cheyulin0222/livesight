package com.arplanets.corexrapi.livesight.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceOrgMemberDto {

    private Long id;

    private String orgId;

    private String uuid;

    private String nickname;

    private Boolean active;

    private String arn;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;
}
