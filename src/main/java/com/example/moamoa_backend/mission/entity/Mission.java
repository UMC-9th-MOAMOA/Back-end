package com.example.moamoa_backend.mission.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private Integer reward;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String description;

}
