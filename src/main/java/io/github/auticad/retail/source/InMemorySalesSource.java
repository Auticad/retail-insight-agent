package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Sale;

/** Vendite fisse passate al costruttore: utile nei test, dove i risultati si calcolano a mano. */
public class InMemorySalesSource implements SalesSource {

    private final Sale[] sales;

    public InMemorySalesSource(Sale... sales) {
        this.sales = sales.clone();          // copia difensiva: chi ha passato l'array non può modificarlo dopo
    }

    @Override
    public Sale[] load() {
        return sales.clone();                // copia difensiva anche in uscita
    }

    @Override
    public String description() {
        return "vendite in memoria (" + sales.length + ")";
    }
}
