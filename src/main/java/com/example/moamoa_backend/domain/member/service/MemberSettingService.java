package com.example.moamoa_backend.domain.member.service;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.member.entity.MemberSetting;
import com.example.moamoa_backend.domain.member.enums.SettingValue;
import com.example.moamoa_backend.domain.member.repository.MemberRepository;
import com.example.moamoa_backend.domain.member.repository.MemberSettingRepository;
import com.example.moamoa_backend.domain.member.exception.MemberException;
import com.example.moamoa_backend.domain.member.exception.code.MemberErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSettingService {

	private final MemberSettingRepository memberSettingRepository;
	private final MemberRepository memberRepository;

	/**
	 * 팝업/튜토리얼 출력 여부를 수정
	 */
	@Transactional
	public void saveSetting(Long memberId, String settingKey, SettingValue settingValue) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

		memberSettingRepository.findByMemberIdAndSettingKey(memberId, settingKey)
			.ifPresentOrElse(
				setting -> setting.updateValue(settingValue),
				() -> memberSettingRepository.save(
					MemberSetting.builder()
						.member(member)
						.settingKey(settingKey)
						.settingValue(settingValue)
						.build()
				)
			);
	}

	/**
	 * 팝업/튜토리얼 다시 보지 않음 설정
	 */
	@Transactional
	public void dismissPopup(Long memberId, String settingKey) {
		saveSetting(memberId, settingKey, SettingValue.NEVER_SHOW);
	}

	/**
	 * 팝업/튜토리얼 출력 필요 여부 체크
	 * - 데이터 없음: 표시 (true)
	 * - NEVER_SHOW: 미표시 (false)
	 */
	public boolean shouldShowPopup(Long memberId, String settingKey) {
		return memberSettingRepository.findByMemberIdAndSettingKey(memberId, settingKey)
			.map(setting -> setting.getSettingValue() != SettingValue.NEVER_SHOW)
			.orElse(true);
	}
}