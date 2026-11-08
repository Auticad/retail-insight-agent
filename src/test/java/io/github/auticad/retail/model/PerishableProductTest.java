package io.github.auticad.retail.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PerishableProductTest {

    private static final LocalDate SCADENZA = LocalDate.of(2026, 11, 20);

    private static PerishableProduct cioccolatini() {
        return new PerishableProduct("CO-14", "Cioccolatini", "alimentari",
                new BigDecimal("12.90"), new BigDecimal("5.40"), 30, SCADENZA);
    }

    @Test
    void fullPriceWhenMoreThanThreeDaysLeft() {
        assertEquals(new BigDecimal("12.90"), cioccolatini().priceOn(SCADENZA.minusDays(4)));
    }

    @Test
    void eightyPercentWhenTwoOrThreeDaysLeft() {
        PerishableProduct p = cioccolatini();
        assertEquals(new BigDecimal("10.32"), p.priceOn(SCADENZA.minusDays(3)));
        assertEquals(new BigDecimal("10.32"), p.priceOn(SCADENZA.minusDays(2)));
    }

    @Test
    void halfPriceOnLastDayAndDayBefore() {
        PerishableProduct p = cioccolatini();
        assertEquals(new BigDecimal("6.45"), p.priceOn(SCADENZA.minusDays(1)));
        assertEquals(new BigDecimal("6.45"), p.priceOn(SCADENZA));
    }

    @Test
    void zeroPriceAfterExpiry() {
        PerishableProduct p = cioccolatini();
        assertEquals(0, p.priceOn(SCADENZA.plusDays(1)).signum());
        assertTrue(p.isExpiredOn(SCADENZA.plusDays(1)));
        assertFalse(p.isExpiredOn(SCADENZA));
    }

    @Test
    void dynamicDispatchThroughProductVariable() {
        Product p = cioccolatini();                       // tipo della variabile: Product
        assertEquals(new BigDecimal("6.45"), p.priceOn(SCADENZA), "deve girare la versione di PerishableProduct");
        assertTrue(p.describe().contains("scad. 2026-11-20"));
    }

    @Test
    void equalityWithPlainProductIsSymmetric() {
        Product normale = new Product("CO-14", "Cioccolatini", "alimentari", new BigDecimal("12.90"), new BigDecimal("5.40"));
        PerishableProduct deperibile = cioccolatini();
        assertEquals(normale, deperibile);
        assertEquals(deperibile, normale);
        assertEquals(normale.hashCode(), deperibile.hashCode());
    }

    @Test
    void expiryIsMandatory() {
        assertThrows(NullPointerException.class, () -> new PerishableProduct("CO-14", "Cioccolatini", "alimentari",
                new BigDecimal("12.90"), new BigDecimal("5.40"), 1, null));
    }
}
