package com.example.moamoa_backend.wallet.service.command;

import com.example.moamoa_backend.member.entity.Member;
import com.example.moamoa_backend.wallet.entity.Wallet;
import com.example.moamoa_backend.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletCommandServiceImpl implements WalletCommandService {

    private final WalletRepository walletRepository;


    public void createWallet(Member member) {
        if (walletRepository.existsByMember(member)){
            return;
        }
        Wallet newWallet = Wallet.create(member);
        walletRepository.save(newWallet);
    }
}