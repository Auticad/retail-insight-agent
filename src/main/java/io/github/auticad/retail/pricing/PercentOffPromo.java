package io.github.auticad.retail.pricing;

/** Sconto percentuale intero sul totale della riga, da 1 a 99. */
public record PercentOffPromo(int percent) implements Promotion {

    public PercentOffPromo {
        if (percent < 1 || percent > 99) {
            throw new IllegalArgumentException("percentuale fuori intervallo 1-99: " + percent);
        }
    }

    @Override
    public String label() {
        return "-" + percent + "%";
    }
}
