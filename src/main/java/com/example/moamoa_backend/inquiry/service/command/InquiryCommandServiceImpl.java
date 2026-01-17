package com.example.moamoa_backend.inquiry.service.command;


import com.example.moamoa_backend.inquiry.converter.InquiryConverter;
import com.example.moamoa_backend.inquiry.dto.InquiryRequestDTO;
import com.example.moamoa_backend.inquiry.dto.InquiryResponseDTO;
import com.example.moamoa_backend.inquiry.entity.Inquiry;
import com.example.moamoa_backend.inquiry.repository.InquiryRepository;
import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommandServiceImpl implements InquiryCommandService{

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    @Override
    public InquiryResponseDTO.CreateResult create(Long memberId, InquiryRequestDTO.Create request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Inquiry inquiry = InquiryConverter.toEntity(member, request);
        Inquiry saved = inquiryRepository.save(inquiry);

        return InquiryConverter.toCreateResult(saved);
    }
}
