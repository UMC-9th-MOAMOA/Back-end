package com.example.moamoa_backend.interest.service;

import com.example.moamoa_backend.interest.dto.InterestResponse;
import com.example.moamoa_backend.interest.dto.SubInterestResponse;
import com.example.moamoa_backend.interest.exception.InterestException;
import com.example.moamoa_backend.interest.exception.code.InterestErrorCode;
import com.example.moamoa_backend.interest.entity.SubInterest;
import com.example.moamoa_backend.interest.repository.InterestRepository;
import com.example.moamoa_backend.interest.repository.SubInterestRepository;
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
	public List<InterestResponse> getInterests() {
		return interestRepository.findAllByOrderByIdAsc().stream()
			.map(i -> InterestResponse.from(i.getId(), i.getName()))
			.toList();
	}

	@Transactional(readOnly = true)
	public List<SubInterestResponse> getSubInterests(Long interestId) {

		List<SubInterest> subs =
			subInterestRepository.findAllByInterest_IdOrderByIdAsc(interestId);

		if (subs.isEmpty() && !interestRepository.existsById(interestId)) {
			throw new InterestException(InterestErrorCode.INTEREST_NOT_FOUND);
		}

		return subs.stream()
			.map(s -> SubInterestResponse.from(s.getId(), s.getName()))
			.toList();
	}
}
