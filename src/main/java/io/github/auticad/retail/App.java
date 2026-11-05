package io.github.auticad.retail;

import io.github.auticad.retail.service.WeeklyReport;
import io.github.auticad.retail.service.KpiCalculator;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Scanner;

public class App {

    static final String VERSIONE = "0.2.0-SNAPSHOT";

    // Dati di prova per il report settimanale (guida A, giorno 7)
    static final String[] NEGOZI = {"Monza", "Lissone", "Desio"};
    static final int[][] PEZZI_SETTIMANA = {
        {120, 98, 110, 130, 160, 210, 190},
        { 80, 75,  90,  85, 120, 170, 150},
        { 60, 55,  70,  65,  90, 140,   0}
    };

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Retail Insight Agent v" + VERSIONE + " ===");

        int scelta;
        do {
            stampaMenu();
            scelta = leggiIntero(in, "Scelta: ", 0, 3);
            switch (scelta) {
                case 1 -> kpiProdotto(in);
                case 2 -> calcolaSconto(in);
                case 3 -> System.out.print(WeeklyReport.genera(NEGOZI, PEZZI_SETTIMANA));
                case 0 -> System.out.println("Arrivederci.");
                default -> System.out.println("Scelta non prevista: " + scelta);  // non raggiungibile grazie alla validazione
            }
        } while (scelta != 0);
    }

    static void stampaMenu() {
        System.out.println();
        System.out.println("1) KPI di un prodotto");
        System.out.println("2) Calcolo sconto su uno scontrino");
        System.out.println("3) Report settimanale dei negozi");
        System.out.println("0) Esci");
    }

    // ----- voci di menu -----

    static void kpiProdotto(Scanner in) {
        BigDecimal prezzo = leggiImportoPositivo(in, "Prezzo di vendita (€): ");
        BigDecimal costo = leggiImporto(in, "Costo di acquisto (€): ");
        int quantita = leggiIntero(in, "Quantità venduta: ", 1, 1_000_000);

        System.out.println(euro("Fatturato", KpiCalculator.fatturato(prezzo, quantita)));
        System.out.println(euro("Margine", KpiCalculator.margine(prezzo, costo, quantita)));
        System.out.println(String.format(Locale.ITALY, "%-12s %12.2f %%", "Margine %",
                KpiCalculator.marginePercentuale(prezzo, costo)));
        if (costo.compareTo(prezzo) > 0) {
            System.out.println("Attenzione: il costo supera il prezzo, il prodotto è venduto in perdita.");
        }
    }

    static void calcolaSconto(Scanner in) {
        BigDecimal importo = leggiImporto(in, "Importo dello scontrino (€): ");
        int perc = KpiCalculator.scontoPercentuale(importo);
        BigDecimal sconto = KpiCalculator.importoSconto(importo, perc);
        System.out.println(String.format(Locale.ITALY, "Fascia di sconto: %d%%", perc));
        System.out.println(euro("Sconto", sconto));
        System.out.println(euro("Da pagare", importo.subtract(sconto)));
    }

    // ----- input validato (giorni 3, 4, 5) -----

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

    /** Come leggiImporto, ma rifiuta lo zero: serve per il prezzo, che fa da divisore nel margine %. */
    static BigDecimal leggiImportoPositivo(Scanner in, String prompt) {
        BigDecimal valore = leggiImporto(in, prompt);
        while (valore.signum() == 0) {
            System.out.println("Il valore deve essere maggiore di zero.");
            valore = leggiImporto(in, prompt);
        }
        return valore;
    }

    // ----- output -----

    static String euro(String etichetta, BigDecimal valore) {
        return String.format(Locale.ITALY, "%-12s %,12.2f €", etichetta, valore);
    }
}

