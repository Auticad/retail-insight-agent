package io.github.auticad.retail.pricing;

/** Prendi buy pezzi e ne paghi pay (per esempio 3x2). */
public record BuyXPayY(int buy, int pay) implements Promotion {

    public BuyXPayY {
        if (buy < 2 || pay < 1 || pay >= buy) {
            throw new IllegalArgumentException("promozione non valida: prendi " + buy + " paghi " + pay);
        }
    }

    @Override
    public String label() {
        return buy + "x" + pay;
    }
}
