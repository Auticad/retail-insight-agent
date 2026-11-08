package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Store;

/**
 * Report settimanale su una matrice pezzi[negozio][giorno].
 * Rispetto alla guida A riceve Store[] invece di String[] (tappa 8).
 */
public final class WeeklyReport {

    public static final String[] GIORNI = {"lun", "mar", "mer", "gio", "ven", "sab", "dom"};

    private static final int LARGHEZZA_NOME = 14;

    private WeeklyReport() {
    }

    /** Somma di ogni riga: pezzi venduti in settimana da ciascun negozio. */
    public static int[] totaliPerNegozio(int[][] pezzi) {
        int[] totali = new int[pezzi.length];
        for (int n = 0; n < pezzi.length; n++) {
            for (int g = 0; g < pezzi[n].length; g++) {
                totali[n] += pezzi[n][g];
            }
        }
        return totali;
    }

    /** Somma di ogni colonna: pezzi venduti dalla catena in ciascun giorno. */
    public static int[] totaliPerGiorno(int[][] pezzi) {
        int[] totali = new int[GIORNI.length];
        for (int[] riga : pezzi) {
            for (int g = 0; g < riga.length; g++) {
                totali[g] += riga[g];
            }
        }
        return totali;
    }

    /** Indice del valore massimo: serve per recuperare sia il valore sia l'etichetta. */
    public static int indiceMassimo(int[] valori) {
        int iMax = 0;
        for (int i = 1; i < valori.length; i++) {
            if (valori[i] > valori[iMax]) {
                iMax = i;
            }
        }
        return iMax;
    }

    /**
     * Costruisce il report come testo, senza stamparlo: così si può testare.
     * @throws IllegalArgumentException se negozi e righe della matrice non corrispondono
     */
    public static String genera(Store[] negozi, int[][] pezzi) {
        if (negozi.length != pezzi.length) {
            throw new IllegalArgumentException("negozi (" + negozi.length + ") e righe di dati (" + pezzi.length + ") non corrispondono");
        }
        int[] perNegozio = totaliPerNegozio(pezzi);
        int[] perGiorno = totaliPerGiorno(pezzi);
        String separatore = "-".repeat(LARGHEZZA_NOME + 6 * GIORNI.length + 8) + "\n";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-" + LARGHEZZA_NOME + "s", "Negozio"));
        for (String g : GIORNI) {
            sb.append(String.format("%6s", g));
        }
        sb.append(String.format("%8s%n", "Totale"));
        sb.append(separatore);

        for (int n = 0; n < pezzi.length; n++) {
            sb.append(String.format("%-" + LARGHEZZA_NOME + "s", negozi[n].getName()));
            for (int g = 0; g < pezzi[n].length; g++) {
                sb.append(String.format("%6d", pezzi[n][g]));
            }
            sb.append(String.format("%8d%n", perNegozio[n]));
        }

        sb.append(separatore);
        sb.append(String.format("%-" + LARGHEZZA_NOME + "s", "Catena"));
        int totaleCatena = 0;
        for (int t : perGiorno) {
            sb.append(String.format("%6d", t));
            totaleCatena += t;
        }
        sb.append(String.format("%8d%n", totaleCatena));

        int gMax = indiceMassimo(perGiorno);
        int nMax = indiceMassimo(perNegozio);
        Store migliore = negozi[nMax];
        sb.append(String.format("%nGiorno migliore della catena: %s (%d pezzi)%n", GIORNI[gMax], perGiorno[gMax]));
        sb.append(String.format("Negozio migliore: %s, %s (%d pezzi)%n", migliore.getName(), migliore.getCity(), perNegozio[nMax]));
        return sb.toString();
    }
}
