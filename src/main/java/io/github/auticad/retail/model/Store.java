package io.github.auticad.retail.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Punto vendita della catena.
 * Classe e non record perché in futuro potrà cambiare (per esempio il nome commerciale),
 * mentre l'id resta fisso ed è la sua identità.
 */
public final class Store {

    private final String id;
    private String name;
    private final String city;
    private final double sqm;

    public Store(String id, String name, String city, double sqm) {
        this.id = requireText(id, "id");
        this.city = requireText(city, "città");
        setName(name);
        if (sqm <= 0) {
            throw new IllegalArgumentException("superficie non positiva: " + sqm);
        }
        this.sqm = sqm;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public double getSqm() { return sqm; }

    public void setName(String name) {
        this.name = requireText(name, "nome");
    }

    /** Resa al metro quadro: incasso / mq, al centesimo. */
    public BigDecimal revenuePerSqm(BigDecimal revenue) {
        Objects.requireNonNull(revenue, "incasso obbligatorio");
        return revenue.divide(BigDecimal.valueOf(sqm), 2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Store other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Store[" + id + ", " + name + ", " + city + ", " + sqm + " mq]";
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " obbligatorio");
        }
        return value.strip();
    }
}
