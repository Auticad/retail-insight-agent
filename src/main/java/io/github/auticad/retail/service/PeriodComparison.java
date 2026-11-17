package io.github.auticad.retail.service;

import java.math.BigDecimal;

/**
 * Fatturato di un negozio in due periodi e variazione percentuale del secondo rispetto al primo.
 * changePct vale zero quando il primo periodo non ha vendite (la variazione non è calcolabile).
 */
public record PeriodComparison(String storeId, String city, BigDecimal revenue1, BigDecimal revenue2, BigDecimal changePct) {
}
