package com.venkatesh.irctc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PaymentRequest {
    private boolean successful;

    @NotBlank
    @Pattern(regexp = "DEMO_CARD|DEMO_UPI", message = "Choose a supported demo payment method")
    private String paymentMethod;

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
