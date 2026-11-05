package io.github.auticad.retail.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void sellReducesStock() {
        Product p = new Product("FE-01", "Felpa", 39.90, 10);
        p.sell(3);
        assertEquals(7, p.getStock());
    }

    @Test
    void sellMoreThanStockIsRejected() {
        Product p = new Product("FE-01", "Felpa", 39.90, 2);
        assertThrows(IllegalStateException.class, () -> p.sell(5));
        assertEquals(2, p.getStock(), "lo stock non deve cambiare dopo un errore");
    }
}
