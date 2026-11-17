package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Genera vendite pseudo-casuali a prezzo di listino.
 * Con lo stesso seme produce sempre le stesse vendite: i test restano ripetibili.
 */
public class GeneratedSalesSource implements SalesSource {

    private final int count;
    private final long seed;
    private final List<Product> catalog;
    private final List<Store> stores;
    private final LocalDate firstDay;
    private final int days;

    public GeneratedSalesSource(int count, long seed, Collection<? extends Product> catalog,
                                Collection<Store> stores, LocalDate firstDay, int days) {
        if (count < 0) throw new IllegalArgumentException("numero di vendite negativo: " + count);
        if (days <= 0) throw new IllegalArgumentException("il periodo deve durare almeno un giorno");
        if (catalog.isEmpty() || stores.isEmpty()) {
            throw new IllegalArgumentException("servono almeno un prodotto e un negozio");
        }
        this.count = count;
        this.seed = seed;
        this.catalog = List.copyOf(catalog);
        this.stores = List.copyOf(stores);
        this.firstDay = Objects.requireNonNull(firstDay, "data di inizio obbligatoria");
        this.days = days;
    }

    @Override
    public List<Sale> load() {
        Random rnd = new Random(seed);           // nuovo a ogni load: stesse vendite a ogni chiamata
        List<Sale> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Product p = catalog.get(rnd.nextInt(catalog.size()));
            Store s = stores.get(rnd.nextInt(stores.size()));
            LocalDate day = firstDay.plusDays(rnd.nextInt(days));
            int qty = 1 + rnd.nextInt(4);
            out.add(new Sale(s.getId(), p.getSku(), day, qty, p.getPrice()));
        }
        return List.copyOf(out);
    }

    @Override
    public String description() {
        return count + " vendite generate (seme " + seed + ", dal " + firstDay + " per " + days + " giorni)";
    }
}
