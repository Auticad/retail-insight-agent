package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.model.Store;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcoli dei principali indicatori di vendita.
 * Guida A: metodi su prezzo, costo e quantità. Guida B: overload su Product e Sale[].
 */
public final class KpiCalculator {

    private static final BigDecimal CENTO = BigDecimal.valueOf(100);

    private KpiCalculator() {
        // classe di utilità: solo metodi statici, non istanziabile
    }

    // ----- guida A -----

    /** Fatturato = prezzo unitario × quantità. */
    public static BigDecimal fatturato(BigDecimal prezzo, int quantita) {
        return prezzo.multiply(BigDecimal.valueOf(quantita));
    }

    /** Margine lordo in euro = (prezzo − costo) × quantità. */
    public static BigDecimal margine(BigDecimal prezzo, BigDecimal costo, int quantita) {
        return prezzo.subtract(costo).multiply(BigDecimal.valueOf(quantita));
    }

    /**
     * Margine percentuale sul prezzo, a due decimali.
     * @throws IllegalArgumentException se il prezzo è zero o negativo
     */
    public static BigDecimal marginePercentuale(BigDecimal prezzo, BigDecimal costo) {
        if (prezzo.signum() <= 0) {
            throw new IllegalArgumentException("Il prezzo deve essere positivo");
        }
        return prezzo.subtract(costo).multiply(CENTO).divide(prezzo, 2, RoundingMode.HALF_UP);
    }

    /**
     * Percentuale di sconto per fascia di importo:
     * sotto 50 € nessuno sconto, 50-149,99 → 5%, 150-299,99 → 10%, da 300 € → 15%.
     */
    public static int scontoPercentuale(BigDecimal importo) {
        if (importo.compareTo(new BigDecimal("300")) >= 0) {
            return 15;
        } else if (importo.compareTo(new BigDecimal("150")) >= 0) {
            return 10;
        } else if (importo.compareTo(new BigDecimal("50")) >= 0) {
            return 5;
        } else {
            return 0;
        }
    }

    /** Importo dello sconto in euro, arrotondato al centesimo. */
    public static BigDecimal importoSconto(BigDecimal importo, int percentuale) {
        return importo.multiply(BigDecimal.valueOf(percentuale)).divide(CENTO, 2, RoundingMode.HALF_UP);
    }

    // ----- guida B: overload sugli oggetti di dominio -----

    /** Fatturato di un prodotto venduto a prezzo di listino. */
    public static BigDecimal fatturato(Product p, int quantita) {
        return fatturato(p.getPrice(), quantita);
    }

    public static BigDecimal margine(Product p, int quantita) {
        return margine(p.getPrice(), p.getCost(), quantita);
    }

    public static BigDecimal marginePercentuale(Product p) {
        return marginePercentuale(p.getPrice(), p.getCost());
    }

    /** Somma degli importi di tutte le vendite. */
    public static BigDecimal fatturatoTotale(Sale[] vendite) {
        BigDecimal totale = BigDecimal.ZERO;
        for (Sale s : vendite) {
            totale = totale.add(s.amount());           // BigDecimal è immutabile: va riassegnato
        }
        return totale;
    }

    /**
     * Fatturato per negozio, nello stesso ordine dell'array negozi.
     * Le vendite di negozi non presenti nell'array vengono ignorate.
     * (Dal giorno 12 diventerà una Map&lt;String, BigDecimal&gt;.)
     */
    public static BigDecimal[] fatturatoPerNegozio(Sale[] vendite, Store[] negozi) {
        BigDecimal[] totali = new BigDecimal[negozi.length];
        for (int i = 0; i < negozi.length; i++) {
            totali[i] = BigDecimal.ZERO;
        }
        for (Sale s : vendite) {
            int i = indiceNegozio(negozi, s.storeId());
            if (i >= 0) {
                totali[i] = totali[i].add(s.amount());
            }
        }
        return totali;
    }

    private static int indiceNegozio(Store[] negozi, String id) {
        for (int i = 0; i < negozi.length; i++) {
            if (negozi[i].getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
