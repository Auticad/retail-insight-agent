package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Sale;

import java.util.List;

/**
 * Esito di un import: le vendite valide e un messaggio per ogni riga scartata.
 * Raccogliere gli errori invece di fermarsi al primo permette di correggere un file in una volta sola.
 */
public record ImportResult(List<Sale> sales, List<String> errors) {

    public ImportResult {
        sales = List.copyOf(sales);
        errors = List.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
