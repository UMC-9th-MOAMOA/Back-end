package com.example.moamoa_backend.domain.mission.entity.mapping;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.domain.keyword.entity.Keyword;
import com.example.moamoa_backend.domain.mission.entity.Mission;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class MissionKeyword extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mission_id")
	private Mission mission;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "keyword_id")
	private Keyword keyword;

}
