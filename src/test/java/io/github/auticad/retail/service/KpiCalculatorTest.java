package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class KpiCalculatorTest {

    private static final BigDecimal PREZZO = new BigDecimal("24.90");
    private static final BigDecimal COSTO = new BigDecimal("11.20");

    @Test
    void revenueMarginAndMarginPercent() {
        // stessi valori della prova rapida della guida A
        assertEquals(0, new BigDecimal("3486.00").compareTo(KpiCalculator.fatturato(PREZZO, 140)));
        assertEquals(0, new BigDecimal("1918.00").compareTo(KpiCalculator.margine(PREZZO, COSTO, 140)));
        assertEquals(new BigDecimal("55.02"), KpiCalculator.marginePercentuale(PREZZO, COSTO));
    }

    @Test
    void productOverloadsGiveSameResults() {
        Product p = new Product("XX-01", "Prova", "basic", PREZZO, COSTO, 0);
        assertEquals(KpiCalculator.fatturato(PREZZO, 140), KpiCalculator.fatturato(p, 140));
        assertEquals(KpiCalculator.margine(PREZZO, COSTO, 140), KpiCalculator.margine(p, 140));
        assertEquals(new BigDecimal("55.02"), KpiCalculator.marginePercentuale(p));
    }

    @Test
    void marginPercentRejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class, () -> KpiCalculator.marginePercentuale(BigDecimal.ZERO, COSTO));
    }

    @Test
    void discountBracketsAtTheirBoundaries() {
        assertEquals(0, KpiCalculator.scontoPercentuale(new BigDecimal("49.99")));
        assertEquals(5, KpiCalculator.scontoPercentuale(new BigDecimal("50")));
        assertEquals(5, KpiCalculator.scontoPercentuale(new BigDecimal("149.99")));
        assertEquals(10, KpiCalculator.scontoPercentuale(new BigDecimal("150")));
        assertEquals(10, KpiCalculator.scontoPercentuale(new BigDecimal("299.99")));
        assertEquals(15, KpiCalculator.scontoPercentuale(new BigDecimal("300")));
    }

    @Test
    void discountAmountIsRoundedToCents() {
        assertEquals(new BigDecimal("7.50"), KpiCalculator.importoSconto(new BigDecimal("149.99"), 5));
    }

    @Test
    void totalAndPerStoreRevenue() {
        LocalDate d = LocalDate.of(2026, 11, 2);
        Sale[] vendite = {
            new Sale("S1", "FE-01", d, 2, new BigDecimal("39.90")),   // 79,80
            new Sale("S2", "TS-03", d, 3, new BigDecimal("14.90")),   // 44,70
            new Sale("S1", "JE-02", d, 1, new BigDecimal("59.00")),   // 59,00
            new Sale("S9", "JE-02", d, 1, new BigDecimal("59.00"))    // negozio sconosciuto
        };
        Store[] negozi = {
            new Store("S1", "Monza Centro", "Monza", 180),
            new Store("S2", "Lissone", "Lissone", 95)
        };

        assertEquals(0, new BigDecimal("242.50").compareTo(KpiCalculator.fatturatoTotale(vendite)));
        BigDecimal[] perNegozio = KpiCalculator.fatturatoPerNegozio(vendite, negozi);
        assertEquals(0, new BigDecimal("138.80").compareTo(perNegozio[0]));
        assertEquals(0, new BigDecimal("44.70").compareTo(perNegozio[1]));
    }

    @Test
    void noSalesMeansZero() {
        assertEquals(0, KpiCalculator.fatturatoTotale(new Sale[0]).signum());
    }
}
