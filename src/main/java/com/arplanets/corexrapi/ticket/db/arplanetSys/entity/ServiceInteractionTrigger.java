package com.arplanets.corexrapi.ticket.db.arplanetSys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "service_interaction_triggers") // <-- (1) 請確認您的資料表名稱
public class ServiceInteractionTrigger {

    // 我們假設這張表有一個自動增長的主鍵 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long interactionTriggerId;

    @Column(name = "trigger_type") // <-- (2) 欄位名稱 (例如 'ticket')
    private String triggerType;

    @Column(name = "trigger_type_id") // <-- (3) 欄位名稱 (例如 'corexr')
    private String triggerTypeId;

    @Column(name = "interaction_id") // <-- (4) (注意拼字) 根據您的描述
    private long interactionId;
}
