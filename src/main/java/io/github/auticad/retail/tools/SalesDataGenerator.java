package io.github.auticad.retail.tools;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.source.CsvSalesSource;
import io.github.auticad.retail.source.DemoData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Genera tre mesi di vendite (1 settembre - 30 novembre 2026) per i 5 negozi e i 12 prodotti di abbigliamento.
 *
 * Regole: quantità media 3 pezzi per prodotto e giorno, corretta per il peso del negozio e per il giorno
 * della settimana (sabato ×1,8, domenica ×1,4, lunedì ×0,7); prezzo di listino con uno sconto casuale
 * fino al 20% nel 15% delle righe. Con il seme 2026 il file prodotto coincide byte per byte con data/sales.csv.
 *
 * Uso: {@code java ... SalesDataGenerator [file di uscita]}. Il valore predefinito è data/sales-generated.csv,
 * così il file versionato data/sales.csv non viene mai sovrascritto per errore.
 */
public final class SalesDataGenerator {

    public static final long SEED = 2026;
    public static final LocalDate FIRST_DAY = LocalDate.of(2026, 9, 1);
    public static final LocalDate LAST_DAY = LocalDate.of(2026, 11, 30);
    static final Path DEFAULT_OUTPUT = Path.of("data", "sales-generated.csv");

    /** A capo fisso: newLine() userebbe \r\n su Windows e il file cambierebbe a seconda del PC. */
    private static final String EOL = "\n";

    /** Peso dei negozi S1-S5 nell'ordine di DemoData.stores(). */
    private static final double[] STORE_WEIGHT = {1.4, 1.0, 0.8, 1.1, 0.6};

    private SalesDataGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Path out = args.length > 0 ? Path.of(args[0]) : DEFAULT_OUTPUT;
        int rows = generate(out, SEED);
        System.out.printf("%d righe scritte in %s (%d KB)%n", rows, out.toAbsolutePath(), Files.size(out) / 1024);
    }

    /** Scrive il file e restituisce il numero di righe di dati (intestazione esclusa). */
    public static int generate(Path out, long seed) throws IOException {
        List<Store> stores = DemoData.stores();
        List<Product> products = DemoData.catalog();
        if (stores.size() != STORE_WEIGHT.length) {
            throw new IllegalStateException("servono " + STORE_WEIGHT.length + " pesi, uno per negozio");
        }
        Path parent = out.toAbsolutePath().getParent();     // toAbsolutePath: un nome senza cartella non ha parent
        Files.createDirectories(parent);

        Random rnd = new Random(seed);
        int rows = 0;
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write(CsvSalesSource.HEADER);
            w.write(EOL);
            for (LocalDate d = FIRST_DAY; !d.isAfter(LAST_DAY); d = d.plusDays(1)) {
                for (int s = 0; s < stores.size(); s++) {
                    for (Product p : products) {
                        double mean = 3 * STORE_WEIGHT[s] * dayFactor(d.getDayOfWeek());
                        int qty = (int) Math.round(mean + rnd.nextGaussian() * Math.sqrt(mean));
                        if (qty <= 0) {
                            continue;
                        }
                        // qui double va bene: è una simulazione, e il prezzo viene arrotondato al centesimo nel testo
                        double price = p.getPrice().doubleValue();
                        if (rnd.nextDouble() < 0.15) {
                            price *= 1 - rnd.nextDouble() * 0.20;
                        }
                        // Locale.ROOT: punto decimale su qualunque PC, il formato del file non deve cambiare
                        w.write(String.format(Locale.ROOT, "%s;%s;%s;%d;%.2f",
                                stores.get(s).getId(), p.getSku(), d, qty, price));
                        w.write(EOL);
                        rows++;
                    }
                }
            }
        }
        return rows;
    }

    static double dayFactor(DayOfWeek d) {
        return switch (d) {
            case SATURDAY -> 1.8;
            case SUNDAY -> 1.4;
            case MONDAY -> 0.7;
            default -> 1.0;
        };
    }
}
