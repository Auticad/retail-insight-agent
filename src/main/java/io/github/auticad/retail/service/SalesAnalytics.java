package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.repository.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Analisi delle vendite con la Stream API: fatturato per negozio, classifica prodotti,
 * margine per categoria, confronto tra periodi. Ogni metodo riceve un intervallo di date
 * con gli estremi inclusi e restituisce liste immutabili di record.
 *
 * Le vendite con un negozio o uno SKU assenti dalle anagrafiche non entrano nei calcoli
 * e sono elencate da {@link #anomalies()}: un dato sporco viene segnalato, non nascosto.
 */
public class SalesAnalytics {

    private final List<Sale> sales;
    private final Map<String, Product> products;
    private final Map<String, Store> stores;
    private final Set<String> anomalies;

    public SalesAnalytics(Collection<Sale> sales, Repository<Product, String> products, Repository<Store, String> stores) {
        this.products = products.findAll().stream()
                .collect(Collectors.toUnmodifiableMap(Product::getSku, Function.identity()));
        this.stores = stores.findAll().stream()
                .collect(Collectors.toUnmodifiableMap(Store::getId, Function.identity()));
        this.anomalies = sales.stream()
                .filter(s -> !isKnown(s))
                .map(s -> s.storeId() + "/" + s.sku())
                .collect(Collectors.toCollection(TreeSet::new));
        this.sales = sales.stream().filter(this::isKnown).toList();
    }

    private boolean isKnown(Sale s) {
        return products.containsKey(s.sku()) && stores.containsKey(s.storeId());
    }

    /** Coppie negozio/SKU presenti nelle vendite ma non nelle anagrafiche. */
    public Set<String> anomalies() {
        return Set.copyOf(anomalies);
    }

    public int size() {
        return sales.size();
    }

    public Optional<LocalDate> firstDate() {
        return sales.stream().map(Sale::date).min(Comparator.naturalOrder());
    }

    public Optional<LocalDate> lastDate() {
        return sales.stream().map(Sale::date).max(Comparator.naturalOrder());
    }

    // ----- analisi -----

    /** Fatturato, margine e resa al metro quadro di ogni negozio, anche senza vendite; per fatturato decrescente, poi nome. */
    public List<StoreRevenue> revenueByStore(LocalDate from, LocalDate to) {
        Map<String, List<Sale>> byStore = between(from, to).collect(Collectors.groupingBy(Sale::storeId));
        return stores.values().stream()
                .map(st -> {
                    List<Sale> list = byStore.getOrDefault(st.getId(), List.of());
                    BigDecimal revenue = sum(list, Sale::amount);
                    BigDecimal margin = sum(list, this::marginOf);
                    return new StoreRevenue(st.getId(), st.getName(), st.getCity(), revenue, margin,
                            KpiCalculator.percentuale(margin, revenue), st.revenuePerSqm(revenue));
                })
                .sorted(Comparator.comparing(StoreRevenue::revenue).reversed()
                        .thenComparing(StoreRevenue::name))
                .toList();
    }

    /** I primi limit prodotti per la metrica scelta; a parità di valore decide lo SKU. */
    public List<ProductRank> topProducts(LocalDate from, LocalDate to, RankingMetric metric, int limit) {
        Objects.requireNonNull(metric, "metrica obbligatoria");
        if (limit < 1) {
            throw new IllegalArgumentException("limit deve essere almeno 1: " + limit);
        }
        Function<Sale, BigDecimal> value = switch (metric) {     // switch esaustivo sull'enum: niente default
            case REVENUE -> Sale::amount;
            case MARGIN -> this::marginOf;
            case QUANTITY -> s -> BigDecimal.valueOf(s.quantity());
        };
        return between(from, to)
                .collect(Collectors.groupingBy(Sale::sku,
                        Collectors.reducing(BigDecimal.ZERO, value, BigDecimal::add)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(limit)
                .map(e -> new ProductRank(e.getKey(), products.get(e.getKey()).getName(), e.getValue()))
                .toList();
    }

    /** Fatturato, margine e SKU distinti per categoria; per margine percentuale decrescente. */
    public List<CategoryKpi> marginByCategory(LocalDate from, LocalDate to) {
        Map<String, List<Sale>> byCategory = between(from, to)
                .collect(Collectors.groupingBy(s -> products.get(s.sku()).getCategory()));
        return byCategory.entrySet().stream()
                .map(e -> {
                    List<Sale> list = e.getValue();
                    BigDecimal revenue = sum(list, Sale::amount);
                    BigDecimal margin = sum(list, this::marginOf);
                    long skus = list.stream().map(Sale::sku).distinct().count();
                    return new CategoryKpi(e.getKey(), revenue, margin, KpiCalculator.percentuale(margin, revenue), skus);
                })
                .sorted(Comparator.comparing(CategoryKpi::marginPct).reversed()
                        .thenComparing(CategoryKpi::category))
                .toList();
    }

    /**
     * Fatturato di ogni negozio nel periodo 1 e nel periodo 2 e variazione del secondo sul primo;
     * dal calo più forte alla crescita più forte, quindi i negozi da guardare per primi sono in cima.
     */
    public List<PeriodComparison> comparePeriods(LocalDate from1, LocalDate to1, LocalDate from2, LocalDate to2) {
        Map<String, BigDecimal> p1 = revenueByStoreId(from1, to1);
        Map<String, BigDecimal> p2 = revenueByStoreId(from2, to2);
        return stores.values().stream()
                .map(st -> {
                    BigDecimal a = p1.getOrDefault(st.getId(), BigDecimal.ZERO);
                    BigDecimal b = p2.getOrDefault(st.getId(), BigDecimal.ZERO);
                    BigDecimal change = a.signum() == 0
                            ? BigDecimal.ZERO.setScale(2)
                            : KpiCalculator.percentuale(b.subtract(a), a);
                    return new PeriodComparison(st.getId(), st.getCity(), a, b, change);
                })
                .sorted(Comparator.comparing(PeriodComparison::changePct)
                        .thenComparing(PeriodComparison::storeId))
                .toList();
    }

    // ----- supporto -----

    /** Vendite nell'intervallo, estremi inclusi. Lo stream è nuovo a ogni chiamata: uno stream si consuma una volta sola. */
    private Stream<Sale> between(LocalDate from, LocalDate to) {
        Objects.requireNonNull(from, "data iniziale obbligatoria");
        Objects.requireNonNull(to, "data finale obbligatoria");
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("la data finale " + to + " precede quella iniziale " + from);
        }
        return sales.stream().filter(s -> !s.date().isBefore(from) && !s.date().isAfter(to));
    }

    private Map<String, BigDecimal> revenueByStoreId(LocalDate from, LocalDate to) {
        return between(from, to).collect(Collectors.groupingBy(Sale::storeId,
                Collectors.reducing(BigDecimal.ZERO, Sale::amount, BigDecimal::add)));
    }

    /** Margine di una riga: quantità × (prezzo praticato − costo di acquisto). */
    private BigDecimal marginOf(Sale s) {
        BigDecimal cost = products.get(s.sku()).getCost();
        return s.unitPrice().subtract(cost).multiply(BigDecimal.valueOf(s.quantity()));
    }

    private static BigDecimal sum(List<Sale> list, Function<Sale, BigDecimal> f) {
        return list.stream().map(f).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
