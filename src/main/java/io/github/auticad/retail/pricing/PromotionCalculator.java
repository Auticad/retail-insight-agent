package io.github.auticad.retail.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Calcolo del totale di una riga di scontrino con promozione. */
public final class PromotionCalculator {

    private static final BigDecimal CENTO = BigDecimal.valueOf(100);

    private PromotionCalculator() {
    }

    /**
     * Totale da pagare per qty pezzi a unitPrice con la promozione p, al centesimo.
     * Lo switch è esaustivo senza default: il compilatore conosce tutti i tipi permessi.
     * Regola di cassa: una promozione non fa mai pagare più del prezzo pieno.
     */
    public static BigDecimal total(Promotion p, int qty, BigDecimal unitPrice) {
        Objects.requireNonNull(p, "promozione obbligatoria");
        Objects.requireNonNull(unitPrice, "prezzo obbligatorio");
        if (qty <= 0) {
            throw new IllegalArgumentException("quantità non positiva: " + qty);
        }
        BigDecimal listino = unitPrice.multiply(BigDecimal.valueOf(qty));

        BigDecimal totale = switch (p) {
            case PercentOffPromo(int percent) ->
                    listino.multiply(BigDecimal.valueOf(100 - percent)).divide(CENTO, 2, RoundingMode.HALF_UP);
            case BuyXPayY(int buy, int pay) -> {
                int gruppi = qty / buy;
                int resto = qty % buy;
                yield unitPrice.multiply(BigDecimal.valueOf(gruppi * pay + resto));
            }
            case Bundle b when qty >= b.qty() -> {
                int gruppi = qty / b.qty();
                int resto = qty % b.qty();
                yield b.price().multiply(BigDecimal.valueOf(gruppi))
                        .add(unitPrice.multiply(BigDecimal.valueOf(resto)));
            }
            case Bundle b -> listino;                // sotto soglia: prezzo pieno
        };
        return totale.min(listino).setScale(2, RoundingMode.HALF_UP);
    }

    /** Risparmio rispetto al prezzo pieno: mai negativo, grazie alla regola di total. */
    public static BigDecimal saving(Promotion p, int qty, BigDecimal unitPrice) {
        BigDecimal pieno = unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
        return pieno.subtract(total(p, qty, unitPrice));
    }
}
