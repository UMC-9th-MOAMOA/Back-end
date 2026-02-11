package com.example.moamoa_backend.domain.wallet.service.command;

import com.example.moamoa_backend.domain.member.entity.Member;
import com.example.moamoa_backend.domain.wallet.entity.Wallet;
import com.example.moamoa_backend.domain.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletCommandServiceImpl implements WalletCommandService {

	private final WalletRepository walletRepository;

	/**
	 * 회원 가입 시 지갑 생성
	 * @param member 지갑을 생성할 회원 엔티티
	 */
	public void createWallet(Member member) {
		if (walletRepository.existsByMember(member)) {
			return;
		}
		Wallet newWallet = Wallet.create(member);
		walletRepository.save(newWallet);
	}
}