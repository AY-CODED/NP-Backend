package org.admin.npapplication.controller;

import org.admin.npapplication.dto.*;
import org.admin.npapplication.service.PromoCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/promo")
public class PromoCodeController {

    @Autowired
    private PromoCodeService promoCodeService;

    @PostMapping("/validate")
    public ResponseEntity<ValidatePromoCodeResponse> validatePromoCode(
            @Valid @RequestBody ValidatePromoCodeRequest request
    ) {
        ValidatePromoCodeResponse response = promoCodeService.validatePromoCode(request);
        return ResponseEntity.ok(response);
    }
}