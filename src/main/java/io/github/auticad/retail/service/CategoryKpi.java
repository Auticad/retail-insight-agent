package io.github.auticad.retail.service;

import java.math.BigDecimal;

/** Indicatori di una categoria merceologica in un periodo; skus è il numero di prodotti distinti venduti. */
public record CategoryKpi(String category, BigDecimal revenue, BigDecimal margin, BigDecimal marginPct, long skus) {
}
