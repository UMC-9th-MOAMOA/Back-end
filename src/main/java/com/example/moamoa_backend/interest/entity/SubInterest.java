package com.example.moamoa_backend.interest.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_sub_interest_name",
                columnNames = {"interest_id", "name"}
        )
})

public class SubInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id",  nullable = false)
    private Interest interest;

    @Column(nullable = false, unique = false)
    private String name;


}
