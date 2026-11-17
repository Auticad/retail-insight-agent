package io.github.auticad.retail.service;

import java.math.BigDecimal;

/** Fatturato e margine di un negozio in un periodo. */
public record StoreRevenue(String storeId, String name, String city,
                           BigDecimal revenue, BigDecimal margin, BigDecimal marginPct,
                           BigDecimal revenuePerSqm) {
}
