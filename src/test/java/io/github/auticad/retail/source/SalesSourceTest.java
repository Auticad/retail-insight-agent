package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.service.KpiCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesSourceTest {

    private static final LocalDate INIZIO = LocalDate.of(2026, 11, 1);
    private final Product[] catalogo = DemoData.products(INIZIO);
    private final Store[] negozi = DemoData.stores();

    @Test
    void sameSeedGivesSameSales() {
        SalesSource a = new GeneratedSalesSource(200, 42L, catalogo, negozi, INIZIO, 30);
        SalesSource b = new GeneratedSalesSource(200, 42L, catalogo, negozi, INIZIO, 30);
        assertArrayEquals(a.load(), b.load());
        assertArrayEquals(a.load(), a.load(), "due load sulla stessa sorgente devono coincidere");
    }

    @Test
    void differentSeedGivesDifferentSales() {
        Sale[] a = new GeneratedSalesSource(200, 42L, catalogo, negozi, INIZIO, 30).load();
        Sale[] b = new GeneratedSalesSource(200, 7L, catalogo, negozi, INIZIO, 30).load();
        assertNotEquals(KpiCalculator.fatturatoTotale(a), KpiCalculator.fatturatoTotale(b));
    }

    @Test
    void generatedSalesRespectCatalogStoresAndPeriod() {
        Sale[] vendite = new GeneratedSalesSource(500, 1L, catalogo, negozi, INIZIO, 30).load();
        assertEquals(500, vendite.length);
        for (Sale s : vendite) {
            assertTrue(isKnownStore(s.storeId()), "negozio sconosciuto: " + s.storeId());
            Product p = findProduct(s.sku());
            assertNotNull(p, "sku sconosciuto: " + s.sku());
            assertEquals(p.getPrice(), s.unitPrice());
            assertFalse(s.date().isBefore(INIZIO));
            assertTrue(s.date().isBefore(INIZIO.plusDays(30)));
            assertTrue(s.quantity() >= 1 && s.quantity() <= 4);
        }
    }

    @Test
    void inMemorySourceIsProtectedByDefensiveCopies() {
        Sale vendita = new Sale("S1", "FE-01", INIZIO, 2, new BigDecimal("39.90"));
        Sale[] originale = {vendita};
        SalesSource src = new InMemorySalesSource(originale);

        originale[0] = null;                      // modifica dell'array passato al costruttore
        src.load()[0] = null;                     // modifica dell'array restituito
        assertEquals(vendita, src.load()[0]);
    }

    @Test
    void sameCodeWorksWithAnySource() {
        Sale vendita = new Sale("S1", "FE-01", INIZIO, 2, new BigDecimal("39.90"));
        SalesSource[] sorgenti = {
            new InMemorySalesSource(vendita),
            new GeneratedSalesSource(0, 42L, catalogo, negozi, INIZIO, 30)
        };
        assertEquals(0, new BigDecimal("79.80").compareTo(KpiCalculator.fatturatoTotale(sorgenti[0].load())));
        assertEquals(0, KpiCalculator.fatturatoTotale(sorgenti[1].load()).signum());
    }

    private boolean isKnownStore(String id) {
        for (Store s : negozi) {
            if (s.getId().equals(id)) return true;
        }
        return false;
    }

    private Product findProduct(String sku) {
        for (Product p : catalogo) {
            if (p.getSku().equals(sku)) return p;
        }
        return null;
    }
}
