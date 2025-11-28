package com.arplanets.corexrapi.livesight.model.po;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "service_org")
public class Org {

    @Id
    private Long id;

    @Column(name = "org_id")
    private String orgId;

    private String name;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "creator_id")
    private String creatorId;

    private String note;

    private String arn;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;
}
