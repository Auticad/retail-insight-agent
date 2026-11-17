package io.github.auticad.retail.repository;

import java.util.List;
import java.util.Optional;

/**
 * Archivio generico di entità con identificativo di tipo ID.
 * Oggi l'implementazione è in memoria; con JDBC basterà una nuova classe che implementa
 * la stessa interfaccia, senza cambiare chi la usa.
 *
 * @param <T>  tipo dell'entità
 * @param <ID> tipo dell'identificativo
 */
public interface Repository<T, ID> {

    /** Inserisce l'entità o sostituisce quella con lo stesso identificativo; restituisce l'entità salvata. */
    T save(T entity);

    /** Optional vuoto se l'identificativo non esiste: il chiamante è obbligato a gestire il caso. */
    Optional<T> findById(ID id);

    /** Tutte le entità, in ordine di inserimento. La lista restituita non è modificabile. */
    List<T> findAll();

    /** true se l'entità esisteva ed è stata rimossa. */
    boolean deleteById(ID id);

    default boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    default int count() {
        return findAll().size();
    }

    /** Salva tutte le entità ricevute: accetta anche una collezione di un sottotipo (PECS: producer extends). */
    default void saveAll(Iterable<? extends T> entities) {
        for (T e : entities) {
            save(e);
        }
    }
}
