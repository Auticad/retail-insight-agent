package io.github.auticad.retail.source;

import io.github.auticad.retail.exception.DataImportException;
import io.github.auticad.retail.model.Sale;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Legge le vendite da un CSV con separatore ';' e intestazione
 * {@code store_id;sku;date;quantity;unit_price} (lo stesso formato di data/sales.csv).
 *
 * Il file viene letto riga per riga, senza caricarlo tutto in memoria. Le righe non valide
 * vengono scartate e descritte in {@link ImportResult#errors()}; un file mancante o
 * un'intestazione sbagliata invece interrompono l'import con {@link DataImportException}.
 */
public class CsvSalesSource implements SalesSource {

    public static final String HEADER = "store_id;sku;date;quantity;unit_price";
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 5;

    private final Path file;

    public CsvSalesSource(Path file) {
        this.file = Objects.requireNonNull(file, "percorso obbligatorio");
    }

    public Path getFile() {
        return file;
    }

    @Override
    public List<Sale> load() throws DataImportException {
        return read().sales();
    }

    /** Import completo: vendite valide più l'elenco delle righe scartate. */
    public ImportResult read() throws DataImportException {
        List<Sale> sales = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // try-with-resources: il file viene chiuso anche in caso di eccezione
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new DataImportException("file vuoto: " + file, 1, null);
            }
            if (!stripBom(header).strip().equals(HEADER)) {
                throw new DataImportException("intestazione inattesa in " + file + ": '" + header + "'", 1, null);
            }
            String line;
            int n = 1;
            while ((line = reader.readLine()) != null) {
                n++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    sales.add(parse(line));
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    // NumberFormatException è una IllegalArgumentException: basta il primo tipo
                    errors.add("riga " + n + ": " + e.getMessage());
                }
            }
        } catch (NoSuchFileException e) {
            throw new DataImportException("file non trovato: " + file.toAbsolutePath(), e);
        } catch (IOException e) {
            throw new DataImportException("errore di lettura di " + file, e);   // la causa originale resta nella catena
        }
        return new ImportResult(sales, errors);
    }

    /** Converte una riga in Sale; lancia IllegalArgumentException o DateTimeParseException se non è valida. */
    static Sale parse(String line) {
        String[] f = line.split(SEPARATOR, -1);              // -1: conserva i campi vuoti finali
        if (f.length != FIELDS) {
            throw new IllegalArgumentException("attesi " + FIELDS + " campi, trovati " + f.length);
        }
        String storeId = requireField(f[0], "store_id");
        String sku = requireField(f[1], "sku");
        LocalDate date = LocalDate.parse(f[2].strip());
        int quantity = Integer.parseInt(f[3].strip());
        BigDecimal unitPrice = new BigDecimal(f[4].strip());
        return new Sale(storeId, sku, date, quantity, unitPrice);   // il record valida quantità e prezzo
    }

    private static String requireField(String value, String name) {
        String v = value.strip();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("campo " + name + " vuoto");
        }
        return v;
    }

    private static String stripBom(String s) {
        return s.startsWith("﻿") ? s.substring(1) : s;
    }

    @Override
    public String description() {
        return "file " + file;
    }
}
