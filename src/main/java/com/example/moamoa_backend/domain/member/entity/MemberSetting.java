package com.example.moamoa_backend.domain.member.entity;

import com.example.moamoa_backend.domain.member.enums.SettingValue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "member_setting",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "setting_key"})
)
public class MemberSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "setting_key", nullable = false, length = 50)
    private String settingKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_value", nullable = false, length = 20)
    private SettingValue settingValue;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MemberSetting(Member member, String settingKey, SettingValue settingValue) {
        this.member = member;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public void updateValue(SettingValue settingValue) {
        this.settingValue = settingValue;
    }
}