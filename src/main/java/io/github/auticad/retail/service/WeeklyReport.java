package io.github.auticad.retail.service;

/** Report settimanale su una matrice pezzi[negozio][giorno]. */
public final class WeeklyReport {

    public static final String[] GIORNI = {"lun", "mar", "mer", "gio", "ven", "sab", "dom"};

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

    /** Costruisce il report tabellare come testo, senza stamparlo: così si può anche testare. */
    public static String genera(String[] negozi, int[][] pezzi) {
        int[] perNegozio = totaliPerNegozio(pezzi);
        int[] perGiorno = totaliPerGiorno(pezzi);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-9s", "Negozio"));
        for (String g : GIORNI) {
            sb.append(String.format("%6s", g));
        }
        sb.append(String.format("%8s%n", "Totale"));
        sb.append("-".repeat(9 + 6 * GIORNI.length + 8)).append('\n');

        for (int n = 0; n < pezzi.length; n++) {
            sb.append(String.format("%-9s", negozi[n]));
            for (int g = 0; g < pezzi[n].length; g++) {
                sb.append(String.format("%6d", pezzi[n][g]));
            }
            sb.append(String.format("%8d%n", perNegozio[n]));
        }

        sb.append("-".repeat(9 + 6 * GIORNI.length + 8)).append('\n');
        sb.append(String.format("%-9s", "Catena"));
        int totaleCatena = 0;
        for (int t : perGiorno) {
            sb.append(String.format("%6d", t));
            totaleCatena += t;
        }
        sb.append(String.format("%8d%n", totaleCatena));

        int gMax = indiceMassimo(perGiorno);
        int nMax = indiceMassimo(perNegozio);
        sb.append(String.format("%nGiorno migliore della catena: %s (%d pezzi)%n", GIORNI[gMax], perGiorno[gMax]));
        sb.append(String.format("Negozio migliore: %s (%d pezzi)%n", negozi[nMax], perNegozio[nMax]));
        return sb.toString();
    }
}

