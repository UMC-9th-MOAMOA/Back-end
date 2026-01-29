package com.example.moamoa_backend.member.service;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.member.entity.MemberSetting;
import com.example.moamoa_backend.member.enums.SettingValue;
import com.example.moamoa_backend.member.repository.MemberRepository;
import com.example.moamoa_backend.member.repository.MemberSettingRepository;
import com.example.moamoa_backend.member.exception.MemberException;
import com.example.moamoa_backend.member.exception.code.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSettingService {

    private final MemberSettingRepository memberSettingRepository;
    private final MemberRepository memberRepository;

    // 설정 저장 (없으면 생성, 있으면 업데이트)
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

    // 팝업 보여줘야 하는지 확인
    public boolean shouldShowPopup(Long memberId, String settingKey) {
        return memberSettingRepository.findByMemberIdAndSettingKey(memberId, settingKey)
                .map(setting -> setting.getSettingValue() != SettingValue.NEVER_SHOW)
                .orElse(true);  // 레코드 없으면 보여줘야 함
    }
}