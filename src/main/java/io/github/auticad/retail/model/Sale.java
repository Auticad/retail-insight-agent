package io.github.auticad.retail.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Riga di vendita: un dato che non deve cambiare, quindi un record.
 * Stessi campi di data/sales.csv e della tabella sales del database.
 */
public record Sale(String storeId, String sku, LocalDate date, int quantity, BigDecimal unitPrice) {

    // costruttore compatto: valida i parametri prima che diventino campi
    public Sale {
        Objects.requireNonNull(storeId, "storeId obbligatorio");
        Objects.requireNonNull(sku, "sku obbligatorio");
        Objects.requireNonNull(date, "data obbligatoria");
        Objects.requireNonNull(unitPrice, "prezzo obbligatorio");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantità non positiva: " + quantity);
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("prezzo negativo: " + unitPrice);
        }
    }

    /** Importo della riga = quantità × prezzo unitario. */
    public BigDecimal amount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
