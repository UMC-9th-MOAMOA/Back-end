package com.example.moamoa_backend.domain.inquiry.service.query;

import com.example.moamoa_backend.domain.inquiry.converter.InquiryQueryConverter;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryDetailResDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryReqDto;
import com.example.moamoa_backend.domain.inquiry.dto.InquiryQueryResDto;
import com.example.moamoa_backend.domain.inquiry.repository.InquiryRepository;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 문의 조회(내 문의 목록/상세)를 제공하는 Query 서비스.
 */
public class InquiryQueryServiceImpl implements InquiryQueryService {

	private final InquiryRepository inquiryRepository;
	private final MemberRepository memberRepository;

	@Override
	/**
	 * 내 문의 목록을 조회한다.
	 *
	 * 정책
	 * - memberId 존재 검증 후 조회
	 * - size는 기본 10, 최대 50으로 제한
	 * - hasNext 판정을 위해 size+1 형태로 조회된 결과에서 잘라냄
	 * - UI 리스트용 contentPreview/answerPreview는 일정 길이로 잘라서 반환
	 *
	 * @param memberId 회원 ID
	 * @param cond 목록 조회 조건(페이지/사이즈 등)
	 * @return 내 문의 목록 DTO
	 */
	public InquiryQueryResDto.MyInquiryList getMyInquiries(Long memberId, InquiryQueryReqDto.MyInquiryList cond) {

		if (!memberRepository.existsById(memberId)) {
			throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
		}
		List<InquiryQueryResDto.MyInquiryItem> fetched = inquiryRepository.findMyInquiryItems(memberId, cond);

		int size = (cond.size() == null || cond.size() <= 0) ? 10 : Math.min(cond.size(), 50);

		boolean hasNext = fetched.size() > size;
		List<InquiryQueryResDto.MyInquiryItem> items = hasNext ? fetched.subList(0, size) : fetched;

		List<InquiryQueryResDto.MyInquiryItem> refined = items.stream()
			.map(i -> new InquiryQueryResDto.MyInquiryItem(
				i.inquiryId(),
				i.category(),
				i.title(),
				toPreview(i.contentPreview(), 40),
				i.answered(),
				i.createdAt(),
				i.responderName(),
				toPreview(i.answerPreview(), 40)
			))
			.toList();

		return InquiryQueryConverter.toMyInquiryList(refined, hasNext);
	}

	/**
	 * UI 리스트에서 사용할 미리보기 문자열을 생성한다.
	 *
	 * @param content 원문 문자열
	 * @param maxLen 최대 길이
	 * @return 잘린 문자열(필요 시 ... 포함)
	 */
	private String toPreview(String content, int maxLen) {
		if (content == null)
			return null;
		String trimmed = content.trim();
		if (trimmed.length() <= maxLen)
			return trimmed;
		return trimmed.substring(0, maxLen) + "...";
	}

	@Override
	/**
	 * 내 문의 상세를 조회한다.
	 *
	 * @param memberId 회원 ID
	 * @param inquiryId 문의 ID
	 * @return 문의 상세 DTO
	 * @throws MemberException 회원이 존재하지 않는 경우
	 */
	public InquiryDetailResDto.MyInquiryDetail getMyInquiryDetail(Long memberId, Long inquiryId) {

		if (!memberRepository.existsById(memberId)) {
			throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);
		}

		return inquiryRepository.findMyInquiryDetail(memberId, inquiryId);
	}
}
