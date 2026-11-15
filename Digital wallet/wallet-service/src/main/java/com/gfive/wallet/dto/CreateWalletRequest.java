package com.gfive.wallet.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record CreateWalletRequest(@NotBlank String userId, @NotNull @DecimalMin("0.00") BigDecimal openingBalance) {}
