package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CouponValidation {
    VALID("Il Codice sconto è valido"),
    EXPIRED("Il Coupon è scaduto!"),
    MAX_USAGE("Non è più possibile usare questo coupon!"),
    LIMIT_USAGE("Hai già usufruito di questo coupon!"),
    NOT_ELIGIBLE("Coupon non valido per i prodotti scelti!");

    private String message;

}
