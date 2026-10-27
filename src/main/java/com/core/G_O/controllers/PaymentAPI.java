package com.core.G_O.controllers;

import com.core.G_O.DTO.PaymentsDTOs.PaymentReviewDecisionDTO;
import com.core.G_O.DTO.PaymentsDTOs.PaymentReviewResponseDTO;
import com.core.G_O.Exception.AtencIAException;
import com.core.G_O.models.User;
import com.core.G_O.service.JwtService;
import com.core.G_O.service.PaymentService;
import com.core.G_O.service.PaymentService.EvidenceResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentAPI {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentAPI.class);

    private final PaymentService paymentService;
    private final JwtService jwtService;

    public PaymentAPI(PaymentService paymentService, JwtService jwtService) {
        this.paymentService = paymentService;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentReviewResponseDTO>> getPayments(
            @RequestParam(value = "status", required = false) String status,
            @AuthenticationPrincipal User user,
            @RequestHeader("Authorization") String header) {

        requireOwner(user);

        Long businessId = jwtService.getBusinessId(header.substring(7));

        LOG.info("@getPayments API > Getting payments for business {} with status {}", businessId, status);

        return ResponseEntity.ok(paymentService.getPayments(businessId, status));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<PaymentReviewResponseDTO> reviewPayment(
            @PathVariable("id") Long paymentId,
            @Valid @RequestBody PaymentReviewDecisionDTO decision,
            @AuthenticationPrincipal User user,
            @RequestHeader("Authorization") String header) {

        requireOwner(user);

        Long businessId = jwtService.getBusinessId(header.substring(7));

        LOG.info("@reviewPayment API > Reviewing payment {} as {}", paymentId, decision.getStatus());

        return ResponseEntity.ok(paymentService.reviewPayment(
                paymentId, businessId, user, decision.getStatus(), decision.getObservation()));
    }

    @GetMapping("/{id}/evidence")
    public ResponseEntity<?> getEvidence(
            @PathVariable("id") Long paymentId,
            @AuthenticationPrincipal User user,
            @RequestHeader("Authorization") String header) {

        if (user == null) {
            throw new AtencIAException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String token = header.substring(7);
        Long businessId = jwtService.getBusinessId(token);
        boolean owner = isOwner(user);
        Long technicianId = jwtService.getTechnicianId(token);

        EvidenceResponse evidence = paymentService.getEvidence(paymentId, businessId, technicianId, owner);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(evidence.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, max-age=0")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .header("X-Content-Type-Options", "nosniff")
                .header("Referrer-Policy", "no-referrer")
                .body(evidence.resource());
    }

    private void requireOwner(User user) {
        if (!isOwner(user)) {
            throw new AtencIAException(HttpStatus.FORBIDDEN, "Solo el dueño puede revisar pagos");
        }
    }

    private boolean isOwner(User user) {
        return user != null
                && user.getRole() != null
                && "owner".equalsIgnoreCase(user.getRole().getRolName());
    }
}
