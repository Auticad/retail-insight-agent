package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.repository.InMemoryRepository;
import io.github.auticad.retail.repository.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dataset minimo con risultati calcolabili a mano.
 *
 * <pre>
 * #  negozio  sku    data        pz  prezzo  importo  margine
 * 1  S1       FE-01  2026-10-05   2  40,00    80,00    48,00   (costo 16)
 * 2  S1       TS-03  2026-10-06   4  15,00    60,00    40,00   (costo 5)
 * 3  S2       FE-01  2026-10-07   1  36,00    36,00    20,00
 * 4  S2       SC-07  2026-11-03   3  25,00    75,00    45,00   (costo 10)
 * 5  S1       FE-01  2026-11-04   1  40,00    40,00    24,00
 * 6  S9       XX-01  2026-11-04   1  10,00    anomalia: negozio e SKU sconosciuti
 * </pre>
 * S3 non ha vendite: deve comparire comunque, con zero.
 */
final class TestData {

    static final LocalDate OTT_1 = LocalDate.of(2026, 10, 1);
    static final LocalDate OTT_31 = LocalDate.of(2026, 10, 31);
    static final LocalDate NOV_1 = LocalDate.of(2026, 11, 1);
    static final LocalDate NOV_30 = LocalDate.of(2026, 11, 30);

    private TestData() {
    }

    static List<Sale> sales() {
        return List.of(
            sale("S1", "FE-01", "2026-10-05", 2, "40.00"),
            sale("S1", "TS-03", "2026-10-06", 4, "15.00"),
            sale("S2", "FE-01", "2026-10-07", 1, "36.00"),
            sale("S2", "SC-07", "2026-11-03", 3, "25.00"),
            sale("S1", "FE-01", "2026-11-04", 1, "40.00"),
            sale("S9", "XX-01", "2026-11-04", 1, "10.00")
        );
    }

    static Repository<Product, String> products() {
        Repository<Product, String> repo = new InMemoryRepository<>(Product::getSku);
        repo.save(new Product("FE-01", "Felpa", "maglieria", new BigDecimal("40.00"), new BigDecimal("16.00"), 10));
        repo.save(new Product("TS-03", "T-shirt", "basic", new BigDecimal("15.00"), new BigDecimal("5.00"), 10));
        repo.save(new Product("SC-07", "Sciarpa", "accessori", new BigDecimal("25.00"), new BigDecimal("10.00"), 10));
        return repo;
    }

    static Repository<Store, String> stores() {
        Repository<Store, String> repo = new InMemoryRepository<>(Store::getId);
        repo.save(new Store("S1", "Monza Centro", "Monza", 100));
        repo.save(new Store("S2", "Lissone", "Lissone", 50));
        repo.save(new Store("S3", "Desio", "Desio", 80));
        return repo;
    }

    static SalesAnalytics analytics() {
        return new SalesAnalytics(sales(), products(), stores());
    }

    private static Sale sale(String store, String sku, String date, int qty, String price) {
        return new Sale(store, sku, LocalDate.parse(date), qty, new BigDecimal(price));
    }
}
