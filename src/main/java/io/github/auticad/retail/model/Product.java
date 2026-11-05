package io.github.auticad.retail.model;

import java.util.Objects;

public class Product {
    private final String sku;           // final: non cambia dopo la costruzione
    private String name;
    private double price;
    private int stock;

    public Product(String sku, String name, double price, int stock) {
        this.sku = Objects.requireNonNull(sku, "sku obbligatorio");
        setName(name);
        setPrice(price);
        if (stock < 0) throw new IllegalArgumentException("stock negativo");
        this.stock = stock;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("nome vuoto");
        }
        this.name = name.strip();
    }

    public void setPrice(double price) {
        if (price <= 0) throw new IllegalArgumentException("prezzo non positivo: " + price);
        this.price = price;
    }

    // niente setStock: le scorte cambiano solo tramite operazioni di dominio
    public void sell(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("quantità non valida");
        if (qty > stock) throw new IllegalStateException("scorte insufficienti");
        stock -= qty;
    }

    public void restock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("quantità non valida");
        stock += qty;
    }
}
