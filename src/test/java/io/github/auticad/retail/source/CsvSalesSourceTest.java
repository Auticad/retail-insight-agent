package io.github.auticad.retail.source;

import io.github.auticad.retail.exception.DataImportException;
import io.github.auticad.retail.model.Sale;
import io.github.auticad.retail.service.KpiCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CsvSalesSourceTest {

    @TempDir
    Path tmp;

    private Path write(String content) throws IOException {
        Path f = tmp.resolve("sales.csv");
        Files.writeString(f, content, StandardCharsets.UTF_8);
        return f;
    }

    @Test
    void validRowsAreKeptAndBadRowsAreReported() throws Exception {
        Path f = write("""
                store_id;sku;date;quantity;unit_price
                S1;FE-01;2026-11-02;2;39.90
                S2;JE-02;2026-11-02;uno;59.00
                S1;TS-03;2026-13-01;1;14.90

                S3;TS-03;2026-11-03;4;14.90
                S3;TS-03;2026-11-03;4
                S3;TS-03;2026-11-03;0;14.90
                """);
        ImportResult res = new CsvSalesSource(f).read();

        assertEquals(2, res.sales().size());
        assertEquals(new Sale("S1", "FE-01", LocalDate.of(2026, 11, 2), 2, new BigDecimal("39.90")), res.sales().get(0));
        assertEquals(4, res.errors().size());
        assertTrue(res.errors().get(0).startsWith("riga 3:"), res.errors().get(0));
        assertTrue(res.errors().get(1).startsWith("riga 4:"));
        assertTrue(res.errors().get(2).startsWith("riga 7: attesi 5 campi, trovati 4"), "la riga vuota conta nella numerazione");
        assertTrue(res.errors().get(3).contains("quantità non positiva"), "la validazione del record vale anche nell'import");
    }

    @Test
    void headerWithBomIsAccepted() throws Exception {
        Path f = write("﻿" + CsvSalesSource.HEADER + "\nS1;FE-01;2026-11-02;2;39.90\n");
        assertEquals(1, new CsvSalesSource(f).load().size());
    }

    @Test
    void missingFileThrowsCheckedExceptionWithCause() {
        CsvSalesSource src = new CsvSalesSource(tmp.resolve("non-esiste.csv"));
        DataImportException e = assertThrows(DataImportException.class, src::load);
        assertInstanceOf(NoSuchFileException.class, e.getCause(), "la causa originale non va persa");
        assertEquals(DataImportException.NO_LINE, e.getLine());
    }

    @Test
    void wrongHeaderStopsTheImport() throws Exception {
        Path f = write("negozio,sku,data\nS1,FE-01,2026-11-02\n");
        DataImportException e = assertThrows(DataImportException.class, () -> new CsvSalesSource(f).load());
        assertEquals(1, e.getLine());
        assertTrue(e.getMessage().endsWith("(riga 1)"));
    }

    @Test
    void emptyFileIsAnImportError() throws Exception {
        Path f = write("");
        assertThrows(DataImportException.class, () -> new CsvSalesSource(f).load());
    }

    @Test
    void projectDataFileLoadsCompletely() throws Exception {
        Path f = Path.of("data", "sales.csv");
        assumeTrue(Files.exists(f), "data/sales.csv non presente: test saltato");
        ImportResult res = new CsvSalesSource(f).read();
        assertTrue(res.errors().isEmpty(), () -> res.errors().toString());
        assertEquals(5019, res.sales().size());
        List<Sale> sales = res.sales();
        assertEquals(0, new BigDecimal("866593.63").compareTo(KpiCalculator.fatturatoTotale(sales)));
    }
}
