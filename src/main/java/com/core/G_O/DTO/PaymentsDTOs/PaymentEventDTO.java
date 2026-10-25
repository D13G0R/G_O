package com.core.G_O.DTO.PaymentsDTOs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDTO {

    private String type;
    private PaymentReviewResponseDTO payment;
}
