package io.github.auticad.retail.model;

import java.time.LocalDate;

public record Sale(String storeId, String sku, LocalDate date, int quantity, double unitPrice) {

    // costruttore compatto: validazione senza riassegnare i campi
    public Sale {
        if (quantity <= 0) throw new IllegalArgumentException("quantità non positiva");
        if (unitPrice < 0) throw new IllegalArgumentException("prezzo negativo");
    }

    public double amount() {
        return quantity * unitPrice;
    }
}
