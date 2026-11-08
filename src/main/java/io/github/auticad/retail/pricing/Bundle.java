package io.github.auticad.retail.pricing;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/** Prezzo fisso per una confezione di qty pezzi (per esempio 2 a 50 €); sotto soglia si paga il listino. */
public record Bundle(int qty, BigDecimal price) implements Promotion {

    public Bundle {
        Objects.requireNonNull(price, "prezzo obbligatorio");
        if (qty < 2) {
            throw new IllegalArgumentException("un bundle richiede almeno 2 pezzi: " + qty);
        }
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("prezzo del bundle non positivo: " + price);
        }
    }

    @Override
    public String label() {
        return String.format(Locale.ITALY, "%d a %.2f €", qty, price);
    }
}
