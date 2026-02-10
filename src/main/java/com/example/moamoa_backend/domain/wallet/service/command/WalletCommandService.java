package com.example.moamoa_backend.domain.wallet.service.command;

import com.example.moamoa_backend.domain.member.entity.Member;

public interface WalletCommandService {

    /**
     * 회원 가입 시 지갑 생성
     * @param member 지갑을 생성할 회원 엔티티
     */
    void createWallet(Member member);
}