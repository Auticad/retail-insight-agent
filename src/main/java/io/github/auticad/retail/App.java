package io.github.auticad.retail;

import io.github.auticad.retail.model.PerishableProduct;
import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.pricing.BuyXPayY;
import io.github.auticad.retail.pricing.Bundle;
import io.github.auticad.retail.pricing.PercentOffPromo;
import io.github.auticad.retail.pricing.Promotion;
import io.github.auticad.retail.pricing.PromotionCalculator;
import io.github.auticad.retail.service.KpiCalculator;
import io.github.auticad.retail.service.WeeklyReport;
import io.github.auticad.retail.source.DemoData;
import io.github.auticad.retail.source.GeneratedSalesSource;
import io.github.auticad.retail.source.SalesSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Scanner;

public class App {

    static final String VERSIONE = "0.3.0-SNAPSHOT";

    /** Data di riferimento per i prezzi dei deperibili. */
    static final LocalDate OGGI = LocalDate.now();

    // Lo stato vive per tutta l'esecuzione: una vendita al banco riduce davvero le scorte.
    static final Store[] NEGOZI = DemoData.stores();
    static final Product[] CATALOGO = DemoData.products(OGGI);
    static final SalesSource VENDITE_MESE =
            new GeneratedSalesSource(600, 42L, CATALOGO, NEGOZI, LocalDate.of(2026, 11, 1), 30);

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Retail Insight Agent v" + VERSIONE + " ===");

        int scelta;
        do {
            stampaMenu();
            scelta = leggiIntero(in, "Scelta: ", 0, 7);
            switch (scelta) {
                case 1 -> stampaCatalogo();
                case 2 -> kpiProdotto(in);
                case 3 -> calcolaSconto(in);
                case 4 -> System.out.print(WeeklyReport.genera(NEGOZI, DemoData.weeklyPieces()));
                case 5 -> venditePerNegozio();
                case 6 -> simulaPromozioni(in);
                case 7 -> venditaAlBanco(in);
                case 0 -> System.out.println("Arrivederci.");
                default -> System.out.println("Scelta non prevista: " + scelta);  // non raggiungibile grazie alla validazione
            }
        } while (scelta != 0);
    }

    static void stampaMenu() {
        System.out.println();
        System.out.println("1) Catalogo prodotti");
        System.out.println("2) KPI di un prodotto");
        System.out.println("3) Calcolo sconto su uno scontrino");
        System.out.println("4) Report settimanale dei negozi");
        System.out.println("5) Fatturato del mese per negozio");
        System.out.println("6) Simulatore promozioni");
        System.out.println("7) Vendita al banco");
        System.out.println("0) Esci");
    }

    // ----- voci di menu -----

    static void stampaCatalogo() {
        System.out.printf("Prezzi del %s%n", OGGI);
        System.out.printf("%3s  %-6s %-24s %-11s %9s %9s %8s %7s%n",
                "#", "SKU", "Nome", "Categoria", "Listino", "Oggi", "Marg. %", "Scorte");
        for (int i = 0; i < CATALOGO.length; i++) {
            Product p = CATALOGO[i];
            String nota = (p instanceof PerishableProduct pp) ? "  scade " + pp.getExpiry() : "";
            System.out.println(String.format(Locale.ITALY, "%3d  %-6s %-24s %-11s %9.2f %9.2f %8.2f %7d%s",
                    i + 1, p.getSku(), p.getName(), p.getCategory(), p.getPrice(), p.priceOn(OGGI),
                    KpiCalculator.marginePercentuale(p), p.getStock(), nota));
        }
    }

    static void kpiProdotto(Scanner in) {
        Product p = scegliProdotto(in);
        int quantita = leggiIntero(in, "Quantità venduta: ", 1, 1_000_000);

        System.out.println(p.describe());
        System.out.println(euro("Fatturato", KpiCalculator.fatturato(p, quantita)));
        System.out.println(euro("Margine", KpiCalculator.margine(p, quantita)));
        System.out.println(String.format(Locale.ITALY, "%-12s %12.2f %%", "Margine %", KpiCalculator.marginePercentuale(p)));
        System.out.println(euro("Magazzino", p.stockValue()) + "  (valore al costo di " + p.getStock() + " pezzi)");
    }

    static void calcolaSconto(Scanner in) {
        BigDecimal importo = leggiImporto(in, "Importo dello scontrino (€): ");
        int perc = KpiCalculator.scontoPercentuale(importo);
        BigDecimal sconto = KpiCalculator.importoSconto(importo, perc);
        System.out.println(String.format(Locale.ITALY, "Fascia di sconto: %d%%", perc));
        System.out.println(euro("Sconto", sconto));
        System.out.println(euro("Da pagare", importo.subtract(sconto)));
    }

    static void venditePerNegozio() {
        // App conosce solo l'interfaccia: domani VENDITE_MESE potrà leggere da CSV o database
        Sale[] vendite = VENDITE_MESE.load();
        BigDecimal[] perNegozio = KpiCalculator.fatturatoPerNegozio(vendite, NEGOZI);

        System.out.println("Sorgente: " + VENDITE_MESE.description());
        System.out.printf("%-14s %14s %8s %12s%n", "Negozio", "Fatturato", "mq", "€ al mq");
        for (int i = 0; i < NEGOZI.length; i++) {
            Store s = NEGOZI[i];
            System.out.println(String.format(Locale.ITALY, "%-14s %,14.2f %8.0f %,12.2f",
                    s.getName(), perNegozio[i], s.getSqm(), s.revenuePerSqm(perNegozio[i])));
        }
        System.out.println(String.format(Locale.ITALY, "%-14s %,14.2f", "Catena", KpiCalculator.fatturatoTotale(vendite)));
    }

    static void simulaPromozioni(Scanner in) {
        Product p = scegliProdotto(in);
        int quantita = leggiIntero(in, "Quantità: ", 1, 1_000);
        BigDecimal prezzo = p.priceOn(OGGI);             // polimorfismo: i deperibili hanno già il loro prezzo
        if (prezzo.signum() == 0) {
            System.out.println("Prodotto scaduto: non vendibile.");
            return;
        }

        Promotion[] promozioni = {
            new PercentOffPromo(20),
            new BuyXPayY(3, 2),
            new Bundle(2, prezzo.multiply(new BigDecimal("1.70")).setScale(2, RoundingMode.HALF_UP))
        };

        System.out.println(p.describe() + ", " + quantita + " pezzi a " + formatoEuro(prezzo));
        System.out.println(euro("Senza promo", prezzo.multiply(BigDecimal.valueOf(quantita))));
        Promotion migliore = null;
        BigDecimal minimo = null;
        for (Promotion promo : promozioni) {
            BigDecimal totale = PromotionCalculator.total(promo, quantita, prezzo);
            System.out.println(euro(promo.label(), totale) + "  risparmio " + formatoEuro(PromotionCalculator.saving(promo, quantita, prezzo)));
            if (minimo == null || totale.compareTo(minimo) < 0) {
                minimo = totale;
                migliore = promo;
            }
        }
        System.out.println("Promozione più conveniente: " + migliore.label());
    }

    static void venditaAlBanco(Scanner in) {
        Product p = scegliProdotto(in);
        BigDecimal prezzo = p.priceOn(OGGI);
        if (p.getStock() == 0) {
            System.out.println("Scorte esaurite per " + p.describe() + ".");
            return;
        }
        if (prezzo.signum() == 0) {
            System.out.println("Prodotto scaduto: non vendibile.");
            return;
        }
        // il massimo è la scorta: sell non riceverà mai una quantità che lo farebbe fallire
        int quantita = leggiIntero(in, "Quantità (max " + p.getStock() + "): ", 1, p.getStock());
        p.sell(quantita);
        System.out.println(euro("Incasso", prezzo.multiply(BigDecimal.valueOf(quantita))));
        System.out.println("Scorte rimaste di " + p.getSku() + ": " + p.getStock());
    }

    static Product scegliProdotto(Scanner in) {
        int n = leggiIntero(in, "Numero del prodotto (1-" + CATALOGO.length + ", vedi catalogo): ", 1, CATALOGO.length);
        return CATALOGO[n - 1];
    }

    // ----- input validato (guida A) -----

    /** Legge una riga non vuota, ripetendo la domanda finché serve. */
    static String leggiRiga(Scanner in, String prompt) {
        String riga;
        do {
            System.out.print(prompt);
            riga = in.nextLine().strip();
        } while (riga.isEmpty());
        return riga;
    }

    /** Intero tra min e max; rifiuta testo e valori fuori intervallo senza terminare il programma. */
    static int leggiIntero(Scanner in, String prompt, int min, int max) {
        while (true) {
            String riga = leggiRiga(in, prompt);
            if (!riga.matches("\\d{1,9}")) {            // solo cifre: parseInt non può fallire
                System.out.println("Inserisci un numero intero.");
                continue;
            }
            int valore = Integer.parseInt(riga);
            if (valore >= min && valore <= max) {
                return valore;
            }
            System.out.printf("Il valore deve essere tra %d e %d.%n", min, max);
        }
    }

    /** Importo non negativo, con virgola o punto decimale (es. 24,90 oppure 24.90). */
    static BigDecimal leggiImporto(Scanner in, String prompt) {
        while (true) {
            String riga = leggiRiga(in, prompt).replace(',', '.');
            if (riga.matches("\\d{1,9}(\\.\\d{1,4})?")) {
                return new BigDecimal(riga);
            }
            System.out.println("Formato non valido: usa ad esempio 24,90 (senza separatore delle migliaia).");
        }
    }

    // ----- output -----

    static String euro(String etichetta, BigDecimal valore) {
        return String.format(Locale.ITALY, "%-12s %,12.2f €", etichetta, valore);
    }

    static String formatoEuro(BigDecimal valore) {
        return String.format(Locale.ITALY, "%,.2f €", valore);
    }
}
