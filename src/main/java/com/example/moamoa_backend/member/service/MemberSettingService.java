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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSettingService {

    private final MemberSettingRepository memberSettingRepository;
    private final MemberRepository memberRepository;

    /**
     * 팝업/튜토리얼 출력 여부를 수정
     * SHOWN, NEVER_SHOW 선택가능
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
    public void banPopup(Long memberId, String settingKey) {
        saveSetting(memberId, settingKey, SettingValue.NEVER_SHOW);
    }

    /**
     * 팝업/튜토리얼 출력 필요 여부 체크
     */
    @Transactional
    public boolean checkAndInitPopup(Long memberId, String settingKey) {
        Optional<MemberSetting> settingOpt = memberSettingRepository.findByMemberIdAndSettingKey(memberId, settingKey);

        if (settingOpt.isPresent()) {
            return settingOpt.get().getSettingValue() != SettingValue.NEVER_SHOW;
        }

        // 없는경우 팝업을 통해 보여줄 것이기 때문에 SHOWN으로 미리 생성
        memberSettingRepository.save(
                MemberSetting.builder()
                        .member(memberRepository.getReferenceById(memberId))
                        .settingKey(settingKey)
                        .settingValue(SettingValue.SHOWN)
                        .build()
        );
        return true;
    }
}