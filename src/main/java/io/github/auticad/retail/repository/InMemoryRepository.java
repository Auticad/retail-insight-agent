package io.github.auticad.retail.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Repository in memoria basato su una LinkedHashMap, che conserva l'ordine di inserimento.
 * L'identificativo non è un campo fisso: lo estrae la funzione passata al costruttore,
 * per esempio {@code Product::getSku} o {@code Store::getId}.
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final Map<ID, T> data = new LinkedHashMap<>();
    private final Function<? super T, ? extends ID> idOf;

    public InMemoryRepository(Function<? super T, ? extends ID> idOf) {
        this.idOf = Objects.requireNonNull(idOf, "funzione identificativo obbligatoria");
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entità obbligatoria");
        ID id = Objects.requireNonNull(idOf.apply(entity), "identificativo nullo");
        data.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(data.values());
    }

    @Override
    public boolean deleteById(ID id) {
        return data.remove(id) != null;
    }

    @Override
    public boolean existsById(ID id) {
        return data.containsKey(id);
    }

    @Override
    public int count() {
        return data.size();
    }
}
