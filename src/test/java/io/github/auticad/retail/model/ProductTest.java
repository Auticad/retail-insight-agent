package io.github.auticad.retail.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private static Product felpa(int stock) {
        return new Product("FE-01", "Felpa", "maglieria", new BigDecimal("39.90"), new BigDecimal("16.00"), stock);
    }

    @Test
    void sellReducesStock() {
        Product p = felpa(10);
        p.sell(3);
        assertEquals(7, p.getStock());
    }

    @Test
    void sellMoreThanStockIsRejected() {
        Product p = felpa(2);
        assertThrows(IllegalStateException.class, () -> p.sell(5));
        assertEquals(2, p.getStock(), "lo stock non deve cambiare dopo un errore");
    }

    @Test
    void sellAndRestockRejectNonPositiveQuantities() {
        Product p = felpa(5);
        assertThrows(IllegalArgumentException.class, () -> p.sell(0));
        assertThrows(IllegalArgumentException.class, () -> p.restock(-1));
    }

    @Test
    void restockAddsToStock() {
        Product p = felpa(5);
        p.restock(20);
        assertEquals(25, p.getStock());
    }

    @Test
    void shortConstructorStartsWithZeroStock() {
        Product p = new Product("TS-03", "T-shirt", "basic", new BigDecimal("14.90"), new BigDecimal("5.20"));
        assertEquals(0, p.getStock());
    }

    @Test
    void constructorRejectsInvalidData() {
        BigDecimal prezzo = new BigDecimal("10");
        BigDecimal costo = new BigDecimal("4");
        assertThrows(IllegalArgumentException.class, () -> new Product(" ", "Felpa", "maglieria", prezzo, costo, 1));
        assertThrows(IllegalArgumentException.class, () -> new Product("FE-01", "", "maglieria", prezzo, costo, 1));
        assertThrows(IllegalArgumentException.class, () -> new Product("FE-01", "Felpa", "maglieria", BigDecimal.ZERO, costo, 1));
        assertThrows(IllegalArgumentException.class, () -> new Product("FE-01", "Felpa", "maglieria", prezzo, new BigDecimal("-1"), 1));
        assertThrows(IllegalArgumentException.class, () -> new Product("FE-01", "Felpa", "maglieria", prezzo, costo, -1));
        assertThrows(NullPointerException.class, () -> new Product("FE-01", "Felpa", "maglieria", null, costo, 1));
    }

    @Test
    void nameIsStrippedAndValidated() {
        Product p = felpa(1);
        p.setName("  Felpa con cappuccio  ");
        assertEquals("Felpa con cappuccio", p.getName());
        assertThrows(IllegalArgumentException.class, () -> p.setName("   "));
        assertEquals("Felpa con cappuccio", p.getName(), "un nome non valido non deve sostituire quello vecchio");
    }

    @Test
    void stockValueUsesCost() {
        assertEquals(new BigDecimal("160.00"), felpa(10).stockValue());
    }

    @Test
    void regularProductPriceDoesNotDependOnDate() {
        Product p = felpa(1);
        assertEquals(new BigDecimal("39.90"), p.priceOn(LocalDate.of(2026, 12, 31)));
    }

    @Test
    void productsWithSameSkuAreEqual() {
        Product a = felpa(10);
        Product b = new Product("FE-01", "Felpa blu", "maglieria", new BigDecimal("42.00"), new BigDecimal("17.00"), 0);
        Product c = new Product("JE-02", "Jeans", "denim", new BigDecimal("59.00"), new BigDecimal("27.50"), 10);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode(), "oggetti uguali devono avere lo stesso hashCode");
        assertNotEquals(a, c);
        assertNotEquals(null, a);
    }
}
