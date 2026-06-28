package com.fooddelivery.dto.response;

import com.fooddelivery.entities.Payment;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class PaymentResponseDTO {
    private Integer id;
    private String paymentMethod;
    private String status;
    private Double amount;
    private String transactionRef;
    private LocalDateTime processedAt;
    private Integer orderId;

    public static PaymentResponseDTO fromEntity(Payment p) {
        return PaymentResponseDTO.builder()
                .id(p.getId())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .amount(p.getAmount())
                .transactionRef(p.getTransactionRef())
                .processedAt(p.getProcessedAt())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .build();
    }
}
