package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KpiCalculatorTest {

    private static final BigDecimal PREZZO = new BigDecimal("24.90");
    private static final BigDecimal COSTO = new BigDecimal("11.20");

    @Test
    void revenueMarginAndMarginPercent() {
        // 24,90 € di prezzo, 11,20 € di costo, 140 pezzi
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
    void totalPiecesAndPerStoreRevenueWithStreams() {
        LocalDate d = LocalDate.of(2026, 11, 2);
        List<Sale> vendite = List.of(
            new Sale("S2", "TS-03", d, 3, new BigDecimal("14.90")),   // 44,70
            new Sale("S1", "FE-01", d, 2, new BigDecimal("39.90")),   // 79,80
            new Sale("S1", "JE-02", d, 1, new BigDecimal("59.00"))    // 59,00
        );
        assertEquals(0, new BigDecimal("183.50").compareTo(KpiCalculator.fatturatoTotale(vendite)));
        assertEquals(6, KpiCalculator.pezziTotali(vendite));

        Map<String, BigDecimal> perNegozio = KpiCalculator.fatturatoPerNegozio(vendite);
        assertEquals(List.of("S1", "S2"), List.copyOf(perNegozio.keySet()), "chiavi in ordine di codice");
        assertEquals(0, new BigDecimal("138.80").compareTo(perNegozio.get("S1")));
        assertEquals(0, new BigDecimal("44.70").compareTo(perNegozio.get("S2")));
    }

    @Test
    void noSalesMeansZero() {
        assertEquals(0, KpiCalculator.fatturatoTotale(List.of()).signum());
        assertTrue(KpiCalculator.fatturatoPerNegozio(List.of()).isEmpty());
    }

    @Test
    void percentageOfZeroTotalIsZero() {
        assertEquals(new BigDecimal("0.00"), KpiCalculator.percentuale(BigDecimal.TEN, BigDecimal.ZERO));
        assertEquals(new BigDecimal("33.33"), KpiCalculator.percentuale(BigDecimal.ONE, new BigDecimal("3")));
    }
}
