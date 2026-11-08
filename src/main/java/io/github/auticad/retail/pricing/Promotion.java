package io.github.auticad.retail.pricing;

/**
 * Tipi di promozione della catena: un insieme chiuso, quindi sealed.
 * Se si aggiunge un tipo alla lista permits, ogni switch che non lo gestisce smette di compilare.
 */
public sealed interface Promotion permits PercentOffPromo, BuyXPayY, Bundle {

    /** Etichetta per menu e scontrino, per esempio «3x2» o «-20%». */
    String label();
}
