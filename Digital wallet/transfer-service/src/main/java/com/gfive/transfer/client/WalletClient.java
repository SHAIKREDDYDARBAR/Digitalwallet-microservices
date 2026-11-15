package com.gfive.transfer.client;
import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal;
@FeignClient(name="wallet-service") public interface WalletClient {
 record AmountRequest(String userId, BigDecimal amount){}
 @PostMapping("/api/wallets/internal/debit") Object debit(@RequestBody AmountRequest request);
 @PostMapping("/api/wallets/internal/credit") Object credit(@RequestBody AmountRequest request);
}
