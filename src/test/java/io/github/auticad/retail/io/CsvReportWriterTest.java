package io.github.auticad.retail.io;

import io.github.auticad.retail.service.StoreRevenue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvReportWriterTest {

    @TempDir
    Path tmp;

    @Test
    void fieldsAreQuotedOnlyWhenNeeded() {
        assertEquals("Felpa", CsvReportWriter.escape("Felpa"));
        assertEquals("\"Felpa; cotone\"", CsvReportWriter.escape("Felpa; cotone"));
        assertEquals("\"Felpa \"\"Classic\"\"\"", CsvReportWriter.escape("Felpa \"Classic\""));
    }

    @Test
    void numbersUseCommaWithoutThousandsSeparator() {
        assertEquals("1234,50", CsvReportWriter.number(new BigDecimal("1234.5")));
    }

    @Test
    void storeReportStartsWithBomAndCreatesFolders() throws IOException {
        Path out = tmp.resolve("output").resolve("negozi.csv");
        StoreRevenue r = new StoreRevenue("S1", "Monza Centro", "Monza", new BigDecimal("180.00"),
                new BigDecimal("112.00"), new BigDecimal("62.22"), new BigDecimal("1.80"));
        CsvReportWriter.writeStoreRevenue(List.of(r), out);

        List<String> lines = Files.readAllLines(out, StandardCharsets.UTF_8);
        assertTrue(lines.get(0).startsWith("﻿negozio;nome;citta;"));
        assertEquals("S1;Monza Centro;Monza;180,00;112,00;62,22;1,80", lines.get(1));
    }
}
