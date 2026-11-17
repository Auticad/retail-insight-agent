package io.github.auticad.retail;

import io.github.auticad.retail.exception.DataImportException;
import io.github.auticad.retail.exception.InsufficientStockException;
import io.github.auticad.retail.io.ConsoleIO;
import io.github.auticad.retail.io.CsvReportWriter;
import io.github.auticad.retail.model.PerishableProduct;
import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.pricing.Bundle;
import io.github.auticad.retail.pricing.BuyXPayY;
import io.github.auticad.retail.pricing.PercentOffPromo;
import io.github.auticad.retail.pricing.Promotion;
import io.github.auticad.retail.pricing.PromotionCalculator;
import io.github.auticad.retail.repository.Repository;
import io.github.auticad.retail.service.CategoryKpi;
import io.github.auticad.retail.service.KpiCalculator;
import io.github.auticad.retail.service.PeriodComparison;
import io.github.auticad.retail.service.ProductRank;
import io.github.auticad.retail.service.RankingMetric;
import io.github.auticad.retail.service.SalesAnalytics;
import io.github.auticad.retail.service.StoreRevenue;
import io.github.auticad.retail.service.WeeklyReport;
import io.github.auticad.retail.source.CsvSalesSource;
import io.github.auticad.retail.source.DemoData;
import io.github.auticad.retail.source.GeneratedSalesSource;
import io.github.auticad.retail.source.ImportResult;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Retail Insight Agent: menu da console.
 *
 * App si occupa solo di input, menu e output. Il dominio sta in model, i dati in source e repository,
 * i calcoli in service e pricing. Ogni voce del menu è protetta: un errore viene spiegato
 * e il menu riparte, non termina il programma.
 */
public class App {

    static final String VERSIONE = "0.4.0-SNAPSHOT";
    static final Path FILE_VENDITE = Path.of("data", "sales.csv");
    static final Path CARTELLA_REPORT = Path.of("output");
    private static final int MAX_ERRORI_MOSTRATI = 5;

    private final ConsoleIO io;
    private final LocalDate oggi;
    private final Repository<Product, String> prodotti;
    private final Repository<Store, String> negozi;
    private final List<Sale> vendite;
    private final SalesAnalytics analytics;

    App(ConsoleIO io, LocalDate oggi, Path fileVendite) {
        this.io = io;
        this.oggi = oggi;
        this.prodotti = DemoData.productRepository(oggi);
        this.negozi = DemoData.storeRepository();
        this.vendite = caricaVendite(fileVendite);
        this.analytics = new SalesAnalytics(vendite, prodotti, negozi);
        if (!analytics.anomalies().isEmpty()) {
            io.println("Attenzione: vendite con negozio o SKU sconosciuti, escluse dalle analisi: " + analytics.anomalies());
        }
    }

    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO(new Scanner(System.in), System.out);
        io.println("=== Retail Insight Agent v" + VERSIONE + " ===");
        new App(io, LocalDate.now(), FILE_VENDITE).run();
    }

    /** Carica il CSV; se non è leggibile prosegue con vendite generate, così il menu resta utilizzabile. */
    private List<Sale> caricaVendite(Path file) {
        CsvSalesSource csv = new CsvSalesSource(file);
        try {
            ImportResult res = csv.read();
            io.println(String.format(Locale.ITALY, "%,d vendite caricate da %s, %d righe scartate.",
                    res.sales().size(), file, res.errors().size()));
            res.errors().stream().limit(MAX_ERRORI_MOSTRATI).forEach(e -> io.println("  " + e));
            if (res.errors().size() > MAX_ERRORI_MOSTRATI) {
                io.println("  ... e altre " + (res.errors().size() - MAX_ERRORI_MOSTRATI));
            }
            return res.sales();
        } catch (DataImportException e) {
            GeneratedSalesSource riserva = new GeneratedSalesSource(600, 42L, prodotti.findAll(), negozi.findAll(),
                    LocalDate.of(2026, 11, 1), 30);
            io.println("Impossibile leggere le vendite: " + e.getMessage());
            io.println("Uso " + riserva.description() + ".");
            return riserva.load();
        }
    }

    void run() {
        int scelta = -1;
        while (scelta != 0) {
            stampaMenu();
            try {
                scelta = io.readInt("Scelta: ", 0, 11);
                esegui(scelta);
            } catch (NoSuchElementException e) {
                io.println("Input terminato.");          // Ctrl+Z / Ctrl+D: non c'è più nulla da leggere
                return;
            } catch (IllegalArgumentException | IOException e) {
                io.println("Errore: " + e.getMessage()); // l'operazione fallisce, il menu no
            }
        }
    }

    private void esegui(int scelta) throws IOException {
        switch (scelta) {
            case 1 -> stampaCatalogo();
            case 2 -> kpiProdotto();
            case 3 -> calcolaSconto();
            case 4 -> reportSettimanale();
            case 5 -> fatturatoPerNegozio();
            case 6 -> classificaProdotti();
            case 7 -> marginePerCategoria();
            case 8 -> confrontoPeriodi();
            case 9 -> simulaPromozioni();
            case 10 -> venditaAlBanco();
            case 11 -> esportaReport();
            case 0 -> io.println("Arrivederci.");
            default -> io.println("Scelta non prevista: " + scelta);  // non raggiungibile grazie a readInt
        }
    }

    private void stampaMenu() {
        io.println("");
        io.println(" 1) Catalogo prodotti");
        io.println(" 2) KPI di un prodotto");
        io.println(" 3) Calcolo sconto su uno scontrino");
        io.println(" 4) Report settimanale dei negozi");
        io.println(" 5) Fatturato e margine per negozio");
        io.println(" 6) Classifica prodotti");
        io.println(" 7) Margine per categoria");
        io.println(" 8) Confronto tra due periodi");
        io.println(" 9) Simulatore promozioni");
        io.println("10) Vendita al banco");
        io.println("11) Esporta report in CSV");
        io.println(" 0) Esci");
    }

    // ----- anagrafiche e cassa -----

    private void stampaCatalogo() {
        io.println("Prezzi del " + oggi);
        io.println(String.format("%-6s %-24s %-11s %9s %9s %8s %7s",
                "SKU", "Nome", "Categoria", "Listino", "Oggi", "Marg. %", "Scorte"));
        for (Product p : prodotti.findAll()) {
            String nota = (p instanceof PerishableProduct pp) ? "  scade " + pp.getExpiry() : "";
            io.println(String.format(Locale.ITALY, "%-6s %-24s %-11s %9.2f %9.2f %8.2f %7d%s",
                    p.getSku(), p.getName(), p.getCategory(), p.getPrice(), p.priceOn(oggi),
                    KpiCalculator.marginePercentuale(p), p.getStock(), nota));
        }
    }

    private void kpiProdotto() {
        Product p = scegliProdotto();
        int quantita = io.readInt("Quantità venduta: ", 1, 1_000_000);
        io.println(p.describe());
        io.println(euro("Fatturato", KpiCalculator.fatturato(p, quantita)));
        io.println(euro("Margine", KpiCalculator.margine(p, quantita)));
        io.println(String.format(Locale.ITALY, "%-12s %12.2f %%", "Margine %", KpiCalculator.marginePercentuale(p)));
        io.println(euro("Magazzino", p.stockValue()) + "  (valore al costo di " + p.getStock() + " pezzi)");
    }

    private void calcolaSconto() {
        BigDecimal importo = io.readDecimal("Importo dello scontrino (€): ");
        int perc = KpiCalculator.scontoPercentuale(importo);
        BigDecimal sconto = KpiCalculator.importoSconto(importo, perc);
        io.println(String.format("Fascia di sconto: %d%%", perc));
        io.println(euro("Sconto", sconto));
        io.println(euro("Da pagare", importo.subtract(sconto)));
    }

    private void simulaPromozioni() {
        Product p = scegliProdotto();
        int quantita = io.readInt("Quantità: ", 1, 1_000);
        BigDecimal prezzo = p.priceOn(oggi);             // polimorfismo: i deperibili hanno già il loro prezzo
        if (prezzo.signum() == 0) {
            io.println("Prodotto scaduto: non vendibile.");
            return;
        }
        List<Promotion> promozioni = List.of(
                new PercentOffPromo(20),
                new BuyXPayY(3, 2),
                new Bundle(2, prezzo.multiply(new BigDecimal("1.70")).setScale(2, RoundingMode.HALF_UP)));

        io.println(p.describe() + ", " + quantita + " pezzi a " + formatoEuro(prezzo));
        io.println(euro("Senza promo", prezzo.multiply(BigDecimal.valueOf(quantita))));
        for (Promotion promo : promozioni) {
            io.println(euro(promo.label(), PromotionCalculator.total(promo, quantita, prezzo))
                    + "  risparmio " + formatoEuro(PromotionCalculator.saving(promo, quantita, prezzo)));
        }
        promozioni.stream()
                .min(Comparator.comparing(pr -> PromotionCalculator.total(pr, quantita, prezzo)))
                .ifPresent(best -> io.println("Promozione più conveniente: " + best.label()));
    }

    private void venditaAlBanco() {
        Product p = scegliProdotto();
        BigDecimal prezzo = p.priceOn(oggi);
        if (prezzo.signum() == 0) {
            io.println("Prodotto scaduto: non vendibile.");
            return;
        }
        int quantita = io.readInt("Quantità (disponibili " + p.getStock() + "): ", 1, 1_000);
        try {
            p.sell(quantita);
            io.println(euro("Incasso", prezzo.multiply(BigDecimal.valueOf(quantita))));
            io.println("Scorte rimaste di " + p.getSku() + ": " + p.getStock());
        } catch (InsufficientStockException e) {
            io.println(String.format("Vendita annullata: richiesti %d pezzi di %s, disponibili %d.",
                    e.getRequested(), e.getSku(), e.getAvailable()));
        }
    }

    /** Chiede lo SKU finché non corrisponde a un prodotto: findById restituisce un Optional. */
    private Product scegliProdotto() {
        while (true) {
            String sku = io.readNonBlank("SKU del prodotto (es. FE-01, vedi catalogo): ").toUpperCase(Locale.ROOT);
            Optional<Product> trovato = prodotti.findById(sku);
            if (trovato.isPresent()) {
                return trovato.get();
            }
            io.println("SKU sconosciuto: " + sku);
        }
    }

    // ----- analisi sulle vendite -----

    private void reportSettimanale() {
        LocalDate predefinito = WeeklyReport.lastFullWeekMonday(vendite).orElse(oggi);
        LocalDate giorno = io.readDate("Un giorno della settimana da analizzare", predefinito);
        LocalDate lunedi = giorno.minusDays(giorno.getDayOfWeek().getValue() - 1L);
        io.print(WeeklyReport.genera(vendite, negozi, lunedi));
    }

    private void fatturatoPerNegozio() {
        Periodo periodo = leggiPeriodo();
        List<StoreRevenue> righe = analytics.revenueByStore(periodo.dal(), periodo.al());
        io.println(String.format("%-14s %-9s %14s %13s %8s %10s", "Negozio", "Città", "Fatturato", "Margine", "Marg.%", "€ al mq"));
        for (StoreRevenue r : righe) {
            io.println(String.format(Locale.ITALY, "%-14s %-9s %,14.2f %,13.2f %8.2f %,10.2f",
                    r.name(), r.city(), r.revenue(), r.margin(), r.marginPct(), r.revenuePerSqm()));
        }
        BigDecimal totale = righe.stream().map(StoreRevenue::revenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        io.println(String.format(Locale.ITALY, "%-24s %,14.2f", "Catena", totale));
    }

    private void classificaProdotti() {
        Periodo periodo = leggiPeriodo();
        RankingMetric[] metriche = RankingMetric.values();
        for (int i = 0; i < metriche.length; i++) {
            io.println((i + 1) + ") " + metriche[i].label());
        }
        RankingMetric metrica = metriche[io.readInt("Metrica: ", 1, metriche.length) - 1];
        int n = io.readInt("Quanti prodotti (1-12): ", 1, 12);
        List<ProductRank> top = analytics.topProducts(periodo.dal(), periodo.al(), metrica, n);
        String formato = metrica == RankingMetric.QUANTITY ? "%2d. %-6s %-22s %,12.0f" : "%2d. %-6s %-22s %,12.2f €";
        for (int i = 0; i < top.size(); i++) {
            ProductRank r = top.get(i);
            io.println(String.format(Locale.ITALY, formato, i + 1, r.sku(), r.name(), r.value()));
        }
    }

    private void marginePerCategoria() {
        Periodo periodo = leggiPeriodo();
        io.println(String.format("%-12s %14s %13s %8s %4s", "Categoria", "Fatturato", "Margine", "Marg.%", "SKU"));
        for (CategoryKpi k : analytics.marginByCategory(periodo.dal(), periodo.al())) {
            io.println(String.format(Locale.ITALY, "%-12s %,14.2f %,13.2f %8.2f %4d",
                    k.category(), k.revenue(), k.margin(), k.marginPct(), k.skus()));
        }
    }

    private void confrontoPeriodi() {
        YearMonth ultimo = YearMonth.from(analytics.lastDate().orElse(oggi));
        YearMonth precedente = ultimo.minusMonths(1);
        io.println("Periodo 1");
        LocalDate f1 = io.readDate("  dal", precedente.atDay(1));
        LocalDate t1 = io.readDate("  al", precedente.atEndOfMonth());
        io.println("Periodo 2");
        LocalDate f2 = io.readDate("  dal", ultimo.atDay(1));
        LocalDate t2 = io.readDate("  al", ultimo.atEndOfMonth());
        io.println(String.format("%-7s %-9s %14s %14s %9s", "Negozio", "Città", "Periodo 1", "Periodo 2", "Var. %"));
        for (PeriodComparison c : analytics.comparePeriods(f1, t1, f2, t2)) {
            io.println(String.format(Locale.ITALY, "%-7s %-9s %,14.2f %,14.2f %+9.2f",
                    c.storeId(), c.city(), c.revenue1(), c.revenue2(), c.changePct()));
        }
    }

    private void esportaReport() throws IOException {
        Periodo periodo = leggiPeriodo();
        String suffisso = periodo.dal() + "_" + periodo.al() + ".csv";
        Path negoziCsv = CsvReportWriter.writeStoreRevenue(
                analytics.revenueByStore(periodo.dal(), periodo.al()), CARTELLA_REPORT.resolve("negozi_" + suffisso));
        Path categorieCsv = CsvReportWriter.writeCategoryKpi(
                analytics.marginByCategory(periodo.dal(), periodo.al()), CARTELLA_REPORT.resolve("categorie_" + suffisso));
        io.println("Report scritti: " + negoziCsv.toAbsolutePath());
        io.println("                " + categorieCsv.toAbsolutePath());
    }

    /** Intervallo di date con estremi inclusi. */
    private record Periodo(LocalDate dal, LocalDate al) {
    }

    /** Chiede un intervallo; i valori predefiniti coprono tutti i dati caricati. */
    private Periodo leggiPeriodo() {
        LocalDate dal = io.readDate("Dal", analytics.firstDate().orElse(oggi));
        LocalDate al = io.readDate("Al", analytics.lastDate().orElse(oggi));
        if (al.isBefore(dal)) {
            throw new IllegalArgumentException("la data finale " + al + " precede quella iniziale " + dal);
        }
        return new Periodo(dal, al);
    }

    // ----- output -----

    static String euro(String etichetta, BigDecimal valore) {
        return String.format(Locale.ITALY, "%-12s %,12.2f €", etichetta, valore);
    }

    static String formatoEuro(BigDecimal valore) {
        return String.format(Locale.ITALY, "%,.2f €", valore);
    }
}
