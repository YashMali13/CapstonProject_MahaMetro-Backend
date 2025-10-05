package com.aurionpro.app.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.WalletTransaction;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    
    Page<WalletTransaction> findByWallet_IdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    List<WalletTransaction> findByWallet_User(User user);
}