package com.example.moamoa_backend.member.entity.mapping;

import com.example.moamoa_backend.global.entity.BaseEntity;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.mission.entity.Mission;
import com.example.moamoa_backend.mission.enums.MissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "member_mission",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_mission_member_mission",
                        columnNames = {"member_id", "mission_id"}
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="mission_id")
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus missionStatus;

    @Column(nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    private LocalDateTime rewardAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isContentWatched = false;

    public void changeMissionStatus(MissionStatus missionStatus) {
        this.missionStatus = missionStatus;
    }

    public void markAsSuccess(){
        this.missionStatus = MissionStatus.SUCCESS;
        this.rewardAt = LocalDateTime.now();
    }

    public void changeIsContentWatched(boolean isContentWatched) {
        this.isContentWatched = isContentWatched;
    }

    public void addAttemptCount(){
        this.attemptCount +=1;
    }

}
