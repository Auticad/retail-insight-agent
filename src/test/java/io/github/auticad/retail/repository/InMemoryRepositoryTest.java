package io.github.auticad.retail.repository;

import io.github.auticad.retail.model.PerishableProduct;
import io.github.auticad.retail.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    private final Repository<Product, String> repo = new InMemoryRepository<>(Product::getSku);

    private static Product product(String sku, String name) {
        return new Product(sku, name, "basic", new BigDecimal("10.00"), new BigDecimal("4.00"), 1);
    }

    @Test
    void findByIdPresentAndAbsent() {
        repo.save(product("TS-03", "T-shirt"));
        assertEquals("T-shirt", repo.findById("TS-03").map(Product::getName).orElseThrow());
        assertTrue(repo.findById("XX-99").isEmpty());
        assertTrue(repo.existsById("TS-03"));
    }

    @Test
    void saveWithSameIdReplaces() {
        repo.save(product("TS-03", "T-shirt"));
        repo.save(product("TS-03", "T-shirt bianca"));
        assertEquals(1, repo.count());
        assertEquals("T-shirt bianca", repo.findById("TS-03").orElseThrow().getName());
    }

    @Test
    void findAllKeepsInsertionOrderAndIsUnmodifiable() {
        repo.save(product("PO-09", "Polo"));
        repo.save(product("FE-01", "Felpa"));
        List<Product> all = repo.findAll();
        assertEquals(List.of("PO-09", "FE-01"), all.stream().map(Product::getSku).toList());
        assertThrows(UnsupportedOperationException.class, () -> all.add(product("XX-01", "Altro")));
    }

    @Test
    void deleteById() {
        repo.save(product("TS-03", "T-shirt"));
        assertTrue(repo.deleteById("TS-03"));
        assertFalse(repo.deleteById("TS-03"), "la seconda cancellazione non trova nulla");
        assertEquals(0, repo.count());
    }

    @Test
    void saveAllAcceptsSubtypes() {
        // PECS: una List<PerishableProduct> entra in un Repository<Product, String>
        List<PerishableProduct> deperibili = List.of(new PerishableProduct("CO-14", "Cioccolatini", "alimentari",
                new BigDecimal("12.90"), new BigDecimal("5.40"), 1, LocalDate.of(2026, 12, 1)));
        repo.saveAll(deperibili);
        assertInstanceOf(PerishableProduct.class, repo.findById("CO-14").orElseThrow());
    }

    @Test
    void nullEntityIsRejected() {
        assertThrows(NullPointerException.class, () -> repo.save(null));
    }
}
