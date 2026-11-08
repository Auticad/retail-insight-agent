package io.github.auticad.retail.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PromotionCalculatorTest {

    private static final BigDecimal DIECI = new BigDecimal("10.00");

    @Test
    void percentOff() {
        assertEquals(new BigDecimal("56.00"), PromotionCalculator.total(new PercentOffPromo(20), 7, DIECI));
    }

    @Test
    void buyThreePayTwo() {
        // 7 pezzi: due gruppi da 3 (si pagano 4) più 1 a prezzo pieno = 5 pezzi pagati
        assertEquals(new BigDecimal("50.00"), PromotionCalculator.total(new BuyXPayY(3, 2), 7, DIECI));
    }

    @Test
    void bundleAboveThreshold() {
        // 7 pezzi: tre confezioni da 2 a 15 € più 1 a prezzo pieno
        assertEquals(new BigDecimal("55.00"), PromotionCalculator.total(new Bundle(2, new BigDecimal("15")), 7, DIECI));
    }

    @Test
    void bundleBelowThresholdPaysFullPrice() {
        assertEquals(new BigDecimal("20.00"), PromotionCalculator.total(new Bundle(3, new BigDecimal("25")), 2, DIECI));
    }

    @Test
    void promotionNeverCostsMoreThanFullPrice() {
        Bundle sconveniente = new Bundle(2, new BigDecimal("25"));      // 2 pezzi a 25 € invece di 20 €
        assertEquals(new BigDecimal("20.00"), PromotionCalculator.total(sconveniente, 2, DIECI));
        assertEquals(new BigDecimal("0.00"), PromotionCalculator.saving(sconveniente, 2, DIECI));
    }

    @Test
    void savingIsFullPriceMinusTotal() {
        assertEquals(new BigDecimal("20.00"), PromotionCalculator.saving(new BuyXPayY(3, 2), 7, DIECI));
    }

    @Test
    void percentResultIsRoundedToCents() {
        // 3 × 9,99 = 29,97; −15% = 25,4745 → 25,47
        assertEquals(new BigDecimal("25.47"), PromotionCalculator.total(new PercentOffPromo(15), 3, new BigDecimal("9.99")));
    }

    @Test
    void invalidPromotionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new PercentOffPromo(0));
        assertThrows(IllegalArgumentException.class, () -> new PercentOffPromo(100));
        assertThrows(IllegalArgumentException.class, () -> new BuyXPayY(3, 3));
        assertThrows(IllegalArgumentException.class, () -> new Bundle(1, DIECI));
        assertThrows(IllegalArgumentException.class, () -> PromotionCalculator.total(new BuyXPayY(3, 2), 0, DIECI));
    }

    @Test
    void labels() {
        assertEquals("-20%", new PercentOffPromo(20).label());
        assertEquals("3x2", new BuyXPayY(3, 2).label());
        assertEquals("2 a 15,00 €", new Bundle(2, new BigDecimal("15")).label());
    }
}
