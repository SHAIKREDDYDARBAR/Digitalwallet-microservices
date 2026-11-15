
package com.gfive.transfer.controller;

import com.gfive.transfer.dto.TransferRequest;
import com.gfive.transfer.entity.Transfer;
import com.gfive.transfer.service.TransferService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Transfer> send(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        Transfer transfer = service.send(request, idempotencyKey);

        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/history/{userId}")
    public List<Transfer> history(@PathVariable String userId) {
        return service.history(userId);
    }

    @GetMapping("/{referenceId}")
    public Transfer get(@PathVariable String referenceId) {
        return service.get(referenceId);
    }
}
