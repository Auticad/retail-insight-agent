package io.github.auticad.retail.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Calcoli dei principali indicatori di vendita . */
public final class KpiCalculator {

    private KpiCalculator() {
        // classe di utilità: solo metodi statici, non istanziabile
    }

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
        return prezzo.subtract(costo).multiply(BigDecimal.valueOf(100))
                .divide(prezzo, 2, RoundingMode.HALF_UP);
    }

    /**
     * Percentuale di sconto per fascia di importo:
     * sotto 50 € nessuno sconto, 50-149,99 → 5%, 150-299,99 → 10%, da 300 € → 15%.
     */
    public static int scontoPercentuale(BigDecimal importo) {
        // si parte dalla soglia più alta: niente condizioni doppie
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
        return importo.multiply(BigDecimal.valueOf(percentuale))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
