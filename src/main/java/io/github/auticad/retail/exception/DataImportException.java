package io.github.auticad.retail.exception;

/**
 * Impossibile importare i dati (file mancante, illeggibile, intestazione errata).
 * Checked: chi importa deve decidere cosa fare, per esempio usare un'altra sorgente.
 * Gli errori delle singole righe non la provocano: vengono raccolti e restituiti con i dati validi.
 */
public class DataImportException extends Exception {

    private static final long serialVersionUID = 1L;   // le eccezioni sono Serializable

    /** Valore di line quando l'errore non riguarda una riga precisa del file. */
    public static final int NO_LINE = 0;

    private final int line;

    public DataImportException(String message, Throwable cause) {
        this(message, NO_LINE, cause);
    }

    public DataImportException(String message, int line, Throwable cause) {
        super(line == NO_LINE ? message : message + " (riga " + line + ")", cause);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
