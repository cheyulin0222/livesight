package com.arplanets.jwt.db.arplanetSys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "service_interaction_modules_bind") // <-- (1) (注意拼字) 根據您的描述
public class ServiceInteractionModuleBind {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interaction_id")
    private long interactionId;

    @Column(name = "module_id") // <-- (3) 欄位名稱
    private long moduleId;
}
