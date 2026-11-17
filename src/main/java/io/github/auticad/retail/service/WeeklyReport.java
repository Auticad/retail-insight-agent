package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.repository.Repository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Report di una settimana (da lunedì a domenica) calcolato dalle vendite:
 * pezzi per negozio e giorno, totale pezzi e fatturato.
 * Le righe sono ordinate per fatturato decrescente e, a parità, per nome del negozio.
 */
public final class WeeklyReport {

    public static final List<String> GIORNI = List.of("lun", "mar", "mer", "gio", "ven", "sab", "dom");

    private static final int LARGHEZZA_NOME = 14;

    /** Riga del report: un negozio con i pezzi per giorno della settimana. */
    public record Row(Store store, Map<DayOfWeek, Integer> pieces, int totalPieces, BigDecimal revenue) {

        public int piecesOn(DayOfWeek day) {
            return pieces.getOrDefault(day, 0);
        }
    }

    /** Ordine del report: fatturato decrescente, poi nome. Pubblico per poterlo testare da solo. */
    public static final Comparator<Row> ORDINE =
            Comparator.comparing(Row::revenue).reversed()
                    .thenComparing(r -> r.store().getName());

    private WeeklyReport() {
    }

    /** Lunedì dell'ultima settimana completa presente nei dati (quella che finisce con l'ultima domenica). */
    public static Optional<LocalDate> lastFullWeekMonday(Collection<Sale> sales) {
        return sales.stream()
                .map(Sale::date)
                .max(Comparator.naturalOrder())
                .map(last -> last.getDayOfWeek() == DayOfWeek.SUNDAY
                        ? last
                        : last.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)))
                .map(sunday -> sunday.minusDays(6));
    }

    /** Righe della settimana che inizia da monday, una per ogni negozio dell'anagrafica, già ordinate. */
    public static List<Row> rows(Collection<Sale> sales, Repository<Store, String> stores, LocalDate monday) {
        if (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException(monday + " non è un lunedì");
        }
        LocalDate sunday = monday.plusDays(6);
        Map<String, List<Sale>> byStore = sales.stream()
                .filter(s -> !s.date().isBefore(monday) && !s.date().isAfter(sunday))
                .collect(Collectors.groupingBy(Sale::storeId));

        return stores.findAll().stream()
                .map(st -> {
                    List<Sale> list = byStore.getOrDefault(st.getId(), List.of());
                    Map<DayOfWeek, Integer> perDay = list.stream().collect(Collectors.groupingBy(
                            s -> s.date().getDayOfWeek(),
                            () -> new EnumMap<>(DayOfWeek.class),
                            Collectors.summingInt(Sale::quantity)));
                    return new Row(st, perDay, KpiCalculator.pezziTotali(list), KpiCalculator.fatturatoTotale(list));
                })
                .sorted(ORDINE)
                .toList();
    }

    /** Costruisce il report come testo, senza stamparlo: così si può testare. */
    public static String genera(Collection<Sale> sales, Repository<Store, String> stores, LocalDate monday) {
        List<Row> rows = rows(sales, stores, monday);
        String separatore = "-".repeat(LARGHEZZA_NOME + 6 * GIORNI.size() + 8 + 14) + "\n";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Settimana dal %s al %s%n", monday, monday.plusDays(6)));
        sb.append(String.format("%-" + LARGHEZZA_NOME + "s", "Negozio"));
        GIORNI.forEach(g -> sb.append(String.format("%6s", g)));
        sb.append(String.format("%8s%14s%n", "Pezzi", "Fatturato"));
        sb.append(separatore);

        for (Row r : rows) {
            sb.append(String.format("%-" + LARGHEZZA_NOME + "s", r.store().getName()));
            for (DayOfWeek d : DayOfWeek.values()) {
                sb.append(String.format("%6d", r.piecesOn(d)));
            }
            sb.append(String.format(Locale.ITALY, "%8d%,14.2f%n", r.totalPieces(), r.revenue()));
        }

        Map<DayOfWeek, Integer> perGiorno = new EnumMap<>(DayOfWeek.class);
        rows.forEach(r -> r.pieces().forEach((d, q) -> perGiorno.merge(d, q, Integer::sum)));
        int pezzi = rows.stream().mapToInt(Row::totalPieces).sum();
        BigDecimal fatturato = rows.stream().map(Row::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);

        sb.append(separatore);
        sb.append(String.format("%-" + LARGHEZZA_NOME + "s", "Catena"));
        for (DayOfWeek d : DayOfWeek.values()) {
            sb.append(String.format("%6d", perGiorno.getOrDefault(d, 0)));
        }
        sb.append(String.format(Locale.ITALY, "%8d%,14.2f%n", pezzi, fatturato));

        if (pezzi == 0) {
            sb.append(String.format("%nNessuna vendita in questa settimana: verifica che sia coperta dai dati.%n"));
            return sb.toString();
        }
        DayOfWeek migliore = perGiorno.entrySet().stream()
                .max(Map.Entry.<DayOfWeek, Integer>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))  // a parità, il giorno prima
                .map(Map.Entry::getKey)
                .orElseThrow();
        Row primo = rows.get(0);
        sb.append(String.format("%nGiorno migliore della catena: %s (%d pezzi)%n",
                GIORNI.get(migliore.getValue() - 1), perGiorno.get(migliore)));
        sb.append(String.format(Locale.ITALY, "Negozio migliore: %s, %s (%,.2f €)%n",
                primo.store().getName(), primo.store().getCity(), primo.revenue()));
        return sb.toString();
    }
}
