package io.github.auticad.retail.service;

/** Criterio di ordinamento della classifica prodotti. */
public enum RankingMetric {
    REVENUE("fatturato"),
    MARGIN("margine"),
    QUANTITY("pezzi");

    private final String label;

    RankingMetric(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
