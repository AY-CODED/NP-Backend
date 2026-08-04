package org.admin.npapplication.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCartItemRequest {
    @Min(1)
    private Integer quantity;
}