package com.example.moamoa_backend.mission.mapping;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.interest.entity.SubInterest;
import com.example.moamoa_backend.mission.entity.Mission;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "mission_sub_interest",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mission_sub_interest_mission_sub",
                        columnNames = {"mission_id", "sub_interest_id"}
                )
        }
)
public class MissionSubInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "sub_interest_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private SubInterest subInterest;

    @JoinColumn(name = "mission_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Mission mission;
}
