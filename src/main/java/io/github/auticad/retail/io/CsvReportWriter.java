package io.github.auticad.retail.io;

import io.github.auticad.retail.service.CategoryKpi;
import io.github.auticad.retail.service.StoreRevenue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scrive i report in CSV pensati per Excel in italiano:
 * BOM UTF-8 (lettere accentate corrette), separatore ';', virgola decimale e nessun separatore delle migliaia.
 * I campi di testo che contengono ';', virgolette o a capo vengono racchiusi tra virgolette.
 */
public final class CsvReportWriter {

    private static final char SEP = ';';
    private static final String BOM = "﻿";

    private CsvReportWriter() {
    }

    public static Path writeStoreRevenue(List<StoreRevenue> rows, Path out) throws IOException {
        List<String> lines = rows.stream()
                .map(r -> line(r.storeId(), r.name(), r.city(),
                        number(r.revenue()), number(r.margin()), number(r.marginPct()), number(r.revenuePerSqm())))
                .toList();
        return write(out, line("negozio", "nome", "citta", "fatturato", "margine", "margine_pct", "euro_al_mq"), lines);
    }

    public static Path writeCategoryKpi(List<CategoryKpi> rows, Path out) throws IOException {
        List<String> lines = rows.stream()
                .map(r -> line(r.category(), number(r.revenue()), number(r.margin()), number(r.marginPct()),
                        String.valueOf(r.skus())))
                .toList();
        return write(out, line("categoria", "fatturato", "margine", "margine_pct", "sku"), lines);
    }

    private static Path write(Path out, String header, List<String> lines) throws IOException {
        Path parent = out.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            w.write(BOM + header);
            w.newLine();
            for (String l : lines) {
                w.write(l);
                w.newLine();
            }
        }
        return out;
    }

    static String line(String... fields) {
        return Stream.of(fields).map(CsvReportWriter::escape).collect(Collectors.joining(String.valueOf(SEP)));
    }

    /** Racchiude tra virgolette solo quando serve, raddoppiando le virgolette interne. */
    static String escape(String field) {
        if (field.indexOf(SEP) >= 0 || field.indexOf('"') >= 0 || field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0) {
            return '"' + field.replace("\"", "\"\"") + '"';
        }
        return field;
    }

    /** 1234.5 → "1234,50": formato che Excel in italiano riconosce come numero. */
    static String number(BigDecimal value) {
        return String.format(Locale.ITALY, "%.2f", value);
    }
}
