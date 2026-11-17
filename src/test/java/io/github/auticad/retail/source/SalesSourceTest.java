package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.service.KpiCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SalesSourceTest {

    private static final LocalDate INIZIO = LocalDate.of(2026, 11, 1);
    private final List<Product> catalogo = DemoData.products(INIZIO);
    private final List<Store> negozi = DemoData.stores();

    @Test
    void sameSeedGivesSameSales() {
        SalesSource a = new GeneratedSalesSource(200, 42L, catalogo, negozi, INIZIO, 30);
        SalesSource b = new GeneratedSalesSource(200, 42L, catalogo, negozi, INIZIO, 30);
        assertEquals(load(a), load(b));
        assertEquals(load(a), load(a), "due load sulla stessa sorgente devono coincidere");
    }

    @Test
    void differentSeedGivesDifferentSales() {
        List<Sale> a = new GeneratedSalesSource(200, 42L, catalogo, negozi, INIZIO, 30).load();
        List<Sale> b = new GeneratedSalesSource(200, 7L, catalogo, negozi, INIZIO, 30).load();
        assertNotEquals(KpiCalculator.fatturatoTotale(a), KpiCalculator.fatturatoTotale(b));
    }

    @Test
    void generatedSalesRespectCatalogStoresAndPeriod() {
        Map<String, Product> perSku = catalogo.stream().collect(Collectors.toMap(Product::getSku, Function.identity()));
        Set<String> idNegozi = negozi.stream().map(Store::getId).collect(Collectors.toSet());

        List<Sale> vendite = new GeneratedSalesSource(500, 1L, catalogo, negozi, INIZIO, 30).load();
        assertEquals(500, vendite.size());
        assertTrue(vendite.stream().allMatch(s -> idNegozi.contains(s.storeId())));
        assertTrue(vendite.stream().allMatch(s -> perSku.get(s.sku()).getPrice().equals(s.unitPrice())));
        assertTrue(vendite.stream().allMatch(s -> !s.date().isBefore(INIZIO) && s.date().isBefore(INIZIO.plusDays(30))));
        assertTrue(vendite.stream().mapToInt(Sale::quantity).allMatch(q -> q >= 1 && q <= 4));
    }

    @Test
    void inMemorySourceIsImmutable() {
        Sale vendita = new Sale("S1", "FE-01", INIZIO, 2, new BigDecimal("39.90"));
        List<Sale> originale = new ArrayList<>(List.of(vendita));
        SalesSource src = new InMemorySalesSource(originale);

        originale.clear();                                        // modifica della lista passata al costruttore
        assertEquals(List.of(vendita), load(src));
        assertThrows(UnsupportedOperationException.class, () -> load(src).clear());
    }

    @Test
    void demoDataMatchesDatabaseSeed() {
        assertEquals(List.of("S1", "S2", "S3", "S4", "S5"), negozi.stream().map(Store::getId).toList());
        assertEquals(12, DemoData.catalog().size());
        assertEquals(15, catalogo.size(), "12 capi più 3 deperibili");
        assertEquals(15, DemoData.productRepository(INIZIO).count());
    }

    /** Le sorgenti in memoria non lanciano DataImportException, ma l'interfaccia la dichiara. */
    private static List<Sale> load(SalesSource s) {
        try {
            return s.load();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
