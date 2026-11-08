package io.github.auticad.retail.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Prodotto del catalogo.
 *
 * Invarianti garantite dalla classe: sku e nome non vuoti, prezzo maggiore di zero,
 * costo non negativo, scorte mai negative. Le scorte cambiano solo con sell e restock.
 * L'identità di dominio è lo SKU: due prodotti con lo stesso SKU sono uguali.
 */
public class Product {

    private final String sku;
    private String name;
    private final String category;
    private BigDecimal price;
    private BigDecimal cost;
    private int stock;

    /** Prodotto appena inserito a catalogo, senza scorte (giorno 8: costruttori concatenati). */
    public Product(String sku, String name, String category, BigDecimal price, BigDecimal cost) {
        this(sku, name, category, price, cost, 0);
    }

    public Product(String sku, String name, String category, BigDecimal price, BigDecimal cost, int stock) {
        this.sku = requireText(sku, "sku");
        this.category = requireText(category, "categoria");
        setName(name);
        setPrice(price);
        setCost(cost);
        if (stock < 0) {
            throw new IllegalArgumentException("scorte negative: " + stock);
        }
        this.stock = stock;
    }

    // ----- accesso in lettura -----

    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getCost() { return cost; }
    public int getStock() { return stock; }

    // ----- modifiche validate -----

    // final: sono chiamati dal costruttore, una sottoclasse non deve poterli ridefinire (giorno 10)
    public final void setName(String name) {
        this.name = requireText(name, "nome");
    }

    public final void setPrice(BigDecimal price) {
        Objects.requireNonNull(price, "prezzo obbligatorio");
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("prezzo non positivo: " + price);
        }
        this.price = price;
    }

    public final void setCost(BigDecimal cost) {
        Objects.requireNonNull(cost, "costo obbligatorio");
        if (cost.signum() < 0) {
            throw new IllegalArgumentException("costo negativo: " + cost);
        }
        this.cost = cost;
    }

    // ----- operazioni di dominio: niente setStock -----

    public void sell(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("quantità non valida: " + qty);
        }
        if (qty > stock) {
            throw new IllegalStateException("scorte insufficienti per " + sku + ": disponibili " + stock + ", richiesti " + qty);
        }
        stock -= qty;
    }

    public void restock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("quantità non valida: " + qty);
        }
        stock += qty;
    }

    /**
     * Prezzo di vendita in una certa data. Per un prodotto normale è il prezzo di listino;
     * le sottoclassi (PerishableProduct) lo ridefiniscono: è il punto del polimorfismo.
     */
    public BigDecimal priceOn(LocalDate day) {
        Objects.requireNonNull(day, "data obbligatoria");
        return price;
    }

    /** Valore di magazzino al costo di acquisto, al centesimo. */
    public BigDecimal stockValue() {
        return cost.multiply(BigDecimal.valueOf(stock)).setScale(2, RoundingMode.HALF_UP);
    }

    /** Descrizione breve per menu e report; le sottoclassi la arricchiscono con super.describe(). */
    public String describe() {
        return sku + " " + name;
    }

    // ----- identità: final, così le sottoclassi non possono rompere la simmetria di equals -----

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product other)) return false;
        return sku.equals(other.sku);
    }

    @Override
    public final int hashCode() {
        return sku.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + details() + "]";
    }

    /** Campi mostrati da toString; le sottoclassi aggiungono i propri senza riscrivere tutto. */
    protected String details() {
        return sku + ", " + name + ", " + category + ", prezzo " + price + ", costo " + cost + ", scorte " + stock;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " obbligatorio");
        }
        return value.strip();
    }
}
