package com.gfive.transfer.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record TransferRequest(@NotBlank String senderUserId,@NotBlank String receiverUserId,@NotNull @DecimalMin("0.01") BigDecimal amount) {}
