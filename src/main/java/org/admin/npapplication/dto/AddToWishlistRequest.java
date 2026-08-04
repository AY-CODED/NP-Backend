package org.admin.npapplication.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToWishlistRequest {
    @NotNull
    private Long productId;
}