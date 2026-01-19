package com.example.moamoa_backend.mission.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.quiz.entity.Quiz;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Mission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", length = 500)
    private String description;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private Integer reward;

    @Column(nullable = false)
    private Integer videoLength;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Builder.Default
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL)
    private List<Quiz> quizzes = new ArrayList<>();



}
