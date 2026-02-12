package com.example.moamoa_backend.domain.inquiry.repository;

import com.example.moamoa_backend.domain.inquiry.entity.Inquiry;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long>, InquiryRepositoryCustom {
}
