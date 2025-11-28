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
public class OrgDto {

    private Long id;

    private String orgId;

    private String name;

    private Boolean active;

    private Long scheduleId;

    private String creatorId;

    private String note;

    private String arn;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;
}
