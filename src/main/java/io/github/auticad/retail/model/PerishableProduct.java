package io.github.auticad.retail.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Prodotto con data di scadenza e prezzo dinamico.
 *
 * Scaglioni di prezzo rispetto alla data di vendita:
 * scaduto → 0 (non vendibile), 0-1 giorni alla scadenza → 50%, 2-3 giorni → 80%,
 * oltre → prezzo pieno.
 */
public class PerishableProduct extends Product {

    private static final BigDecimal META = new BigDecimal("0.50");
    private static final BigDecimal OTTANTA_PER_CENTO = new BigDecimal("0.80");

    private final LocalDate expiry;

    public PerishableProduct(String sku, String name, String category,
                             BigDecimal price, BigDecimal cost, int stock, LocalDate expiry) {
        super(sku, name, category, price, cost, stock);    // prima istruzione obbligatoria
        this.expiry = Objects.requireNonNull(expiry, "data di scadenza obbligatoria");
    }

    public LocalDate getExpiry() {
        return expiry;
    }

    public boolean isExpiredOn(LocalDate day) {
        return day.isAfter(expiry);
    }

    @Override
    public BigDecimal priceOn(LocalDate day) {
        BigDecimal listino = super.priceOn(day);              // riusa validazione e listino del padre
        long giorni = ChronoUnit.DAYS.between(day, expiry);
        if (giorni < 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        if (giorni <= 1) {
            return listino.multiply(META).setScale(2, RoundingMode.HALF_UP);
        }
        if (giorni <= 3) {
            return listino.multiply(OTTANTA_PER_CENTO).setScale(2, RoundingMode.HALF_UP);
        }
        return listino;
    }

    @Override
    public String describe() {
        return super.describe() + " (scad. " + expiry + ")";
    }

    @Override
    protected String details() {
        return super.details() + ", scadenza " + expiry;
    }
}
