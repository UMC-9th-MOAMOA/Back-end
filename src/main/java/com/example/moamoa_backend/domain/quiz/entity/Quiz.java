package com.example.moamoa_backend.domain.quiz.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.mission.entity.Mission;
import com.example.moamoa_backend.domain.quiz.enums.QuizType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mission_id")
	private Mission mission;

	@Column(nullable = false)
	private String question;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuizType type;

	@Column(nullable = false)
	private String answer;

	@Column(columnDefinition = "JSON")
	private String detailInformation;

	@Column(columnDefinition = "TEXT")
	private String explanation;

	public void setMission(Mission mission) {
		this.mission = mission;
		if (!mission.getQuizzes().contains(this)) {
			mission.getQuizzes().add(this);
		}
	}
}
