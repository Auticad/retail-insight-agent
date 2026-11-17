package io.github.auticad.retail.source;

import io.github.auticad.retail.model.Sale;

import java.util.Collection;
import java.util.List;

/** Vendite fisse passate al costruttore: utile nei test, dove i risultati si calcolano a mano. */
public class InMemorySalesSource implements SalesSource {

    private final List<Sale> sales;

    public InMemorySalesSource(Collection<Sale> sales) {
        this.sales = List.copyOf(sales);     // copia immutabile: chi ha passato la collezione non può modificarla dopo
    }

    public InMemorySalesSource(Sale... sales) {
        this(List.of(sales));
    }

    @Override
    public List<Sale> load() {               // nessun throws: un'implementazione può dichiarare meno eccezioni
        return sales;                        // già immutabile, nessuna copia necessaria
    }

    @Override
    public String description() {
        return "vendite in memoria (" + sales.size() + ")";
    }
}
