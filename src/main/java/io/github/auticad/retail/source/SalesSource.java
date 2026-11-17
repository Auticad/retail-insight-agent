package io.github.auticad.retail.source;

import io.github.auticad.retail.exception.DataImportException;
import io.github.auticad.retail.model.Sale;

import java.util.List;

/**
 * Sorgente delle vendite. Il resto del programma conosce solo questo contratto:
 * le vendite possono arrivare dalla memoria, da un generatore, da un CSV
 * e in seguito da un database, senza modificare chi le usa.
 */
public interface SalesSource {

    /**
     * Restituisce le vendite in una lista non modificabile.
     * @throws DataImportException se la sorgente non è leggibile (le sorgenti in memoria non la lanciano mai)
     */
    List<Sale> load() throws DataImportException;

    /** Nome leggibile della sorgente, per i messaggi del menu. */
    default String description() {
        return getClass().getSimpleName();
    }
}
