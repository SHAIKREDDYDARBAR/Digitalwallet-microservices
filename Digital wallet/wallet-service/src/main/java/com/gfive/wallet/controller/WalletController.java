
package com.gfive.wallet.controller;

import com.gfive.wallet.dto.*;
import com.gfive.wallet.entity.Wallet;
import com.gfive.wallet.service.WalletService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @PostMapping
    public Wallet create(@Valid @RequestBody CreateWalletRequest r) {
        return service.create(r.userId(), r.openingBalance());
    }

    @GetMapping("/{userId}")
    public Wallet get(@PathVariable String userId) {
        return service.get(userId);
    }

    @PostMapping("/internal/debit")
    public Wallet debit(@Valid @RequestBody AmountRequest r) {
        return service.debit(r.userId(), r.amount());
    }

    @PostMapping("/internal/credit")
    public Wallet credit(@Valid @RequestBody AmountRequest r) {
        return service.credit(r.userId(), r.amount());
    }
}
