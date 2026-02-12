package com.example.moamoa_backend.domain.inquiry.entity;

import com.example.moamoa_backend.global.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InquiryImage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inquiry_id", nullable = false)
	private Inquiry inquiry;

	@Column(nullable = false, length = 1000)
	private String imageUrl;

	@Column(nullable = false)
	private Integer sortOrder;
}
