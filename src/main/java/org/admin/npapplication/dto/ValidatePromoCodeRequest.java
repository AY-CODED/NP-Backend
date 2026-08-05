package org.admin.npapplication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidatePromoCodeRequest {
    @NotBlank
    private String code;

    private BigDecimal orderAmount;
}