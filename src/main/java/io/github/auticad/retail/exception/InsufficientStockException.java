package io.github.auticad.retail.exception;

/**
 * Richiesta di vendita superiore alle scorte disponibili.
 * Unchecked: è un errore di logica che il chiamante può prevenire controllando le scorte,
 * quindi non obbliga ogni metodo a dichiararla.
 */
public class InsufficientStockException extends RuntimeException {

    private static final long serialVersionUID = 1L;   // le eccezioni sono Serializable

    private final String sku;
    private final int requested;
    private final int available;

    public InsufficientStockException(String sku, int requested, int available) {
        super("scorte insufficienti per " + sku + ": richiesti " + requested + ", disponibili " + available);
        this.sku = sku;
        this.requested = requested;
        this.available = available;
    }

    public String getSku() { return sku; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
