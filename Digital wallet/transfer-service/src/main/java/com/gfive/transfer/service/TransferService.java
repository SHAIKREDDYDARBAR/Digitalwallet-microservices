
package com.gfive.transfer.service;

import com.gfive.transfer.client.WalletClient;
import com.gfive.transfer.entity.Transfer;
import com.gfive.transfer.repository.TransferRepository;
import com.gfive.transfer.dto.TransferRequest;

import feign.FeignException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransferService {

    private final TransferRepository repo;
    private final WalletClient wallet;

    public TransferService(
            TransferRepository repo,
            WalletClient wallet) {
        this.repo = repo;
        this.wallet = wallet;
    }

    @Transactional
    public Transfer send(TransferRequest r, String key) {

        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Idempotency-Key is required"
            );
        }

        if (key.length() > 100) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Idempotency-Key is too long"
            );
        }

        if (r.senderUserId().equals(r.receiverUserId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Sender and receiver must be different"
            );
        }

        if (r.amount() == null ||
                r.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Amount must be greater than zero"
            );
        }

        // Check whether this request was already processed
        Transfer existing = repo.findByIdempotencyKey(key)
            .orElse(null);

        if (existing != null) {
            validateSameRequest(existing, r);
            return existing;
        }

        // Create a new transfer
        Transfer tx = new Transfer(
            UUID.randomUUID().toString(),
            key,
            r.senderUserId(),
            r.receiverUserId(),
            r.amount(),
            "PROCESSING"
        );

        try {
            // Save key before performing wallet operations
            tx = repo.saveAndFlush(tx);

        } catch (org.springframework.dao.DataIntegrityViolationException ex) {

            // Another concurrent request may have saved this key
            Transfer duplicate = repo.findByIdempotencyKey(key)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Duplicate request. Please retry."
                    )
                );

            validateSameRequest(duplicate, r);
            return duplicate;
        }

        // Debit sender
        try {
            wallet.debit(
                new WalletClient.AmountRequest(
                    r.senderUserId(),
                    r.amount()
                )
            );

        } catch (FeignException.Conflict ex) {

            tx.setStatus("FAILED");
            repo.saveAndFlush(tx);

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Insufficient balance"
            );
        }

        // Credit receiver
        try {
            wallet.credit(
                new WalletClient.AmountRequest(
                    r.receiverUserId(),
                    r.amount()
                )
            );

            tx.setStatus("SUCCESS");

        } catch (Exception ex) {

            // Compensating refund
            try {
                wallet.credit(
                    new WalletClient.AmountRequest(
                        r.senderUserId(),
                        r.amount()
                    )
                );

                tx.setStatus("REFUNDED");

            } catch (Exception refundError) {

                tx.setStatus("FAILED_MANUAL_REVIEW");
            }
        }

        return repo.save(tx);
    }

    private void validateSameRequest(
            Transfer existing,
            TransferRequest request) {

        boolean same =
            existing.getSenderUserId().equals(request.senderUserId()) &&
            existing.getReceiverUserId().equals(request.receiverUserId()) &&
            existing.getAmount().compareTo(request.amount()) == 0;

        if (!same) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Idempotency key already used with different request details"
            );
        }
    }

    public List<Transfer> history(String userId) {
        return repo.findBySenderUserIdOrReceiverUserIdOrderByCreatedAtDesc(
            userId, userId
        );
    }

    public Transfer get(String ref) {
        return repo.findByReferenceId(ref)
            .orElseThrow(() ->
                new IllegalArgumentException("Transfer not found")
            );
    }
}
