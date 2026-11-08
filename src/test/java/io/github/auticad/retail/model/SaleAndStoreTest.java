package io.github.auticad.retail.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SaleAndStoreTest {

    private static final LocalDate GIORNO = LocalDate.of(2026, 11, 9);

    @Test
    void saleAmountIsQuantityTimesPrice() {
        Sale s = new Sale("S1", "FE-01", GIORNO, 2, new BigDecimal("39.90"));
        assertEquals(new BigDecimal("79.80"), s.amount());
    }

    @Test
    void saleRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Sale("S1", "FE-01", GIORNO, 0, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Sale("S1", "FE-01", GIORNO, 1, new BigDecimal("-0.01")));
        assertThrows(NullPointerException.class, () -> new Sale("S1", "FE-01", null, 1, BigDecimal.TEN));
    }

    @Test
    void recordsWithSameValuesAreEqual() {
        Sale a = new Sale("S1", "FE-01", GIORNO, 2, new BigDecimal("39.90"));
        Sale b = new Sale("S1", "FE-01", GIORNO, 2, new BigDecimal("39.90"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void revenuePerSqmIsRoundedToCents() {
        Store monza = new Store("S1", "Monza Centro", "Monza", 180);
        assertEquals(new BigDecimal("111.22"), monza.revenuePerSqm(new BigDecimal("20019.60")));
    }

    @Test
    void storeRejectsInvalidValuesAndUsesIdAsIdentity() {
        assertThrows(IllegalArgumentException.class, () -> new Store("S1", "Monza", "Monza", 0));
        assertThrows(IllegalArgumentException.class, () -> new Store("", "Monza", "Monza", 100));
        assertEquals(new Store("S1", "Monza Centro", "Monza", 180), new Store("S1", "Monza Duomo", "Monza", 200));
    }
}
