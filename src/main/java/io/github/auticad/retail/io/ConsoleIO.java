package io.github.auticad.retail.io;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Tutte le letture da console del menu passano da qui.
 * Ogni metodo ripete la domanda finché il valore non è valido: un input errato non termina mai il programma.
 * Scanner e PrintStream arrivano dal costruttore, così nei test si usano una stringa e un buffer.
 */
public class ConsoleIO {

    private final Scanner in;
    private final PrintStream out;

    public ConsoleIO(Scanner in, PrintStream out) {
        this.in = Objects.requireNonNull(in);
        this.out = Objects.requireNonNull(out);
    }

    /** Riga non vuota, senza spazi iniziali e finali. */
    public String readNonBlank(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            out.println("Il valore non può essere vuoto.");
        }
    }

    /** Intero compreso tra min e max, estremi inclusi. */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readNonBlank(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                out.printf("Inserisci un valore tra %d e %d.%n", min, max);
            } catch (NumberFormatException e) {
                out.println("'" + line + "' non è un numero intero.");
            }
        }
    }

    /** Importo non negativo; accetta la virgola o il punto come separatore decimale (24,90 o 24.90). */
    public BigDecimal readDecimal(String prompt) {
        while (true) {
            String line = readNonBlank(prompt);
            try {
                BigDecimal value = new BigDecimal(line.replace(',', '.'));
                if (value.signum() >= 0) {
                    return value;
                }
                out.println("Il valore non può essere negativo.");
            } catch (NumberFormatException e) {
                out.println("'" + line + "' non è un importo valido: usa ad esempio 24,90 senza separatore delle migliaia.");
            }
        }
    }

    /** Data nel formato aaaa-mm-gg; con invio a vuoto restituisce il valore predefinito. */
    public LocalDate readDate(String prompt, LocalDate defaultValue) {
        while (true) {
            String line = readLine(prompt + " [" + defaultValue + "]: ");
            if (line.isEmpty()) {
                return defaultValue;
            }
            try {
                return LocalDate.parse(line);
            } catch (DateTimeParseException e) {
                out.println("'" + line + "' non è una data valida: usa il formato aaaa-mm-gg.");
            }
        }
    }

    /** Sì o no; accetta s, si, sì, n, no senza distinzione tra maiuscole e minuscole. */
    public boolean readYesNo(String prompt) {
        while (true) {
            String line = readNonBlank(prompt + " (s/n): ").toLowerCase();
            switch (line) {
                case "s", "si", "sì" -> { return true; }
                case "n", "no" -> { return false; }
                default -> out.println("Rispondi s oppure n.");
            }
        }
    }

    public void println(String text) {
        out.println(text);
    }

    public void print(String text) {
        out.print(text);
    }

    /**
     * Legge una riga grezza.
     * @throws NoSuchElementException se l'input è finito (per esempio Ctrl+Z su Windows):
     *         non c'è modo di chiedere di nuovo, quindi l'errore va propagato
     */
    private String readLine(String prompt) {
        out.print(prompt);
        if (!in.hasNextLine()) {
            throw new NoSuchElementException("input terminato");
        }
        return in.nextLine().strip();
    }
}
