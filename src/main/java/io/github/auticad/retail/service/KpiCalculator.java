package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Sale;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Calcoli dei principali indicatori di vendita.
 * I metodi su prezzo, costo e quantità sono i mattoni; quelli su Sale lavorano con gli stream.
 */
public final class KpiCalculator {

    private static final BigDecimal CENTO = BigDecimal.valueOf(100);

    private KpiCalculator() {
        // classe di utilità: solo metodi statici, non istanziabile
    }

    // ----- prezzo, costo, quantità -----

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
        return percentuale(prezzo.subtract(costo), prezzo);
    }

    /** parte / totale × 100 a due decimali; zero se il totale è zero (nessuna vendita, nessuna divisione). */
    public static BigDecimal percentuale(BigDecimal parte, BigDecimal totale) {
        if (totale.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return parte.multiply(CENTO).divide(totale, 2, RoundingMode.HALF_UP);
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

    // ----- prodotti -----

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

    // ----- vendite, con gli stream -----

    /** Somma degli importi: map + reduce con identità ZERO, quindi zero per una collezione vuota. */
    public static BigDecimal fatturatoTotale(Collection<Sale> vendite) {
        return vendite.stream()
                .map(Sale::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Pezzi venduti in totale. */
    public static int pezziTotali(Collection<Sale> vendite) {
        return vendite.stream().mapToInt(Sale::quantity).sum();
    }

    /** Fatturato per codice negozio, in ordine di codice: l'equivalente di SELECT store_id, SUM(...) GROUP BY store_id. */
    public static Map<String, BigDecimal> fatturatoPerNegozio(Collection<Sale> vendite) {
        return vendite.stream().collect(Collectors.groupingBy(
                Sale::storeId,
                TreeMap::new,
                Collectors.reducing(BigDecimal.ZERO, Sale::amount, BigDecimal::add)));
    }
}
