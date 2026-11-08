package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Sale;

/**
 * Sorgente delle vendite. Il resto del programma conosce solo questo contratto:
 * oggi le vendite sono in memoria o generate, poi arriveranno da CSV 
 * e da database senza modificare chi le usa.
 */
public interface SalesSource {

    /** Restituisce le vendite. Ogni chiamata restituisce un array nuovo, che il chiamante può modificare. */
    Sale[] load();

    /** Nome leggibile della sorgente, per i messaggi del menu. */
    default String description() {
        return getClass().getSimpleName();
    }
}
