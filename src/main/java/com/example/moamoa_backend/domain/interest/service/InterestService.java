package com.example.moamoa_backend.domain.interest.service;

import com.example.moamoa_backend.domain.interest.dto.InterestResponseDto;
import com.example.moamoa_backend.domain.interest.dto.SubInterestResponseDto;
import com.example.moamoa_backend.domain.interest.exception.InterestException;
import com.example.moamoa_backend.domain.interest.exception.code.InterestErrorCode;
import com.example.moamoa_backend.domain.interest.entity.SubInterest;
import com.example.moamoa_backend.domain.interest.repository.InterestRepository;
import com.example.moamoa_backend.domain.interest.repository.SubInterestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestService {

	private final InterestRepository interestRepository;
	private final SubInterestRepository subInterestRepository;

	@Transactional(readOnly = true)
	public List<InterestResponseDto> getInterests() {
		return interestRepository.findAllByOrderByIdAsc().stream()
			.map(i -> InterestResponseDto.from(i.getId(), i.getName()))
			.toList();
	}

	@Transactional(readOnly = true)
	public List<SubInterestResponseDto> getSubInterests(Long interestId) {

		List<SubInterest> subs =
			subInterestRepository.findAllByInterest_IdOrderByIdAsc(interestId);

		if (subs.isEmpty() && !interestRepository.existsById(interestId)) {
			throw new InterestException(InterestErrorCode.INTEREST_NOT_FOUND);
		}

		return subs.stream()
			.map(s -> SubInterestResponseDto.from(s.getId(), s.getName()))
			.toList();
	}
}
