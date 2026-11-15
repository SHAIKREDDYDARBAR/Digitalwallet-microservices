package com.gfive.wallet.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record AmountRequest(@NotBlank String userId, @NotNull @DecimalMin(value="0.01") BigDecimal amount) {}
