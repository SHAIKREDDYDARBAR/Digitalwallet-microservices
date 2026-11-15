
package com.gfive.wallet.service;

import com.gfive.wallet.entity.Wallet;
import com.gfive.wallet.repository.WalletRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository repo;

    public WalletService(WalletRepository repo) {
        this.repo = repo;
    }

    // Create wallet
    @Transactional
    public Wallet create(String userId, BigDecimal openingBalance) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        validateOpeningBalance(openingBalance);

        if (repo.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Wallet already exists");
        }

        Wallet wallet = new Wallet(userId, openingBalance);

        return repo.save(wallet);
    }

    // Get wallet
    public Wallet get(String userId) {

        return repo.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));
    }

    // Debit with pessimistic locking
    @Transactional
    public Wallet debit(String userId, BigDecimal amount) {

        validateAmount(amount);

        Wallet wallet = repo.findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        wallet.setBalance(
                wallet.getBalance().subtract(amount));

        return repo.save(wallet);
    }

    // Credit with pessimistic locking
    @Transactional
    public Wallet credit(String userId, BigDecimal amount) {

        validateAmount(amount);

        Wallet wallet = repo.findByUserIdForUpdate(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        wallet.setBalance(
                wallet.getBalance().add(amount));

        return repo.save(wallet);
    }

    // Validate transaction amount
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive");
        }
    }

    // Validate opening balance
    private void validateOpeningBalance(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Opening balance cannot be negative");
        }
    }
}
