package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;

/**
 * Genera vendite pseudo-casuali a prezzo di listino.
 * Con lo stesso seme produce sempre le stesse vendite: i test restano ripetibili.
 */
public class GeneratedSalesSource implements SalesSource {

    private final int count;
    private final long seed;
    private final Product[] catalog;
    private final Store[] stores;
    private final LocalDate firstDay;
    private final int days;

    public GeneratedSalesSource(int count, long seed, Product[] catalog, Store[] stores,
                                LocalDate firstDay, int days) {
        if (count < 0) throw new IllegalArgumentException("numero di vendite negativo: " + count);
        if (days <= 0) throw new IllegalArgumentException("il periodo deve durare almeno un giorno");
        if (catalog.length == 0 || stores.length == 0) {
            throw new IllegalArgumentException("servono almeno un prodotto e un negozio");
        }
        this.count = count;
        this.seed = seed;
        this.catalog = catalog.clone();
        this.stores = stores.clone();
        this.firstDay = Objects.requireNonNull(firstDay, "data di inizio obbligatoria");
        this.days = days;
    }

    @Override
    public Sale[] load() {
        Random rnd = new Random(seed);           // nuovo a ogni load: stesse vendite a ogni chiamata
        Sale[] out = new Sale[count];
        for (int i = 0; i < count; i++) {
            Product p = catalog[rnd.nextInt(catalog.length)];
            Store s = stores[rnd.nextInt(stores.length)];
            LocalDate day = firstDay.plusDays(rnd.nextInt(days));
            int qty = 1 + rnd.nextInt(4);
            out[i] = new Sale(s.getId(), p.getSku(), day, qty, p.getPrice());
        }
        return out;
    }

    @Override
    public String description() {
        return count + " vendite generate (seme " + seed + ", dal " + firstDay + " per " + days + " giorni)";
    }
}
