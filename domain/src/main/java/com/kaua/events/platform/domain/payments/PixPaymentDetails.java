package com.kaua.events.platform.domain.payments;

import java.math.BigDecimal;
import java.util.Optional;

public final class PixPaymentDetails implements PaymentDetails {

    private final BigDecimal amount;
    private final String qrCode;
    private final String qrCodeImageUrl;
    private final int expiresIn;

    public PixPaymentDetails(
            final BigDecimal aAmount,
            final String aQrCode,
            final String aQrCodeImageUrl,
            final int expiresIn
    ) {
        this.amount = aAmount;
        this.qrCode = aQrCode;
        this.qrCodeImageUrl = aQrCodeImageUrl;
        this.expiresIn = expiresIn;
    }

    public PixPaymentDetails(final BigDecimal aAmount) {
        this(aAmount, null, null, 0);
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.PIX;
    }

    @Override
    public BigDecimal amount() {
        return amount;
    }

    public Optional<String> getQrCode() {
        return Optional.ofNullable(qrCode);
    }

    public Optional<String> getQrCodeImageUrl() {
        return Optional.ofNullable(qrCodeImageUrl);
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}
