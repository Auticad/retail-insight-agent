package io.github.auticad.retail.service;

import java.math.BigDecimal;

/** Posizione di un prodotto in una classifica; value dipende dalla metrica scelta. */
public record ProductRank(String sku, String name, BigDecimal value) {
}
