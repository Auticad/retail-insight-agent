package io.github.auticad.retail.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SalesDataGeneratorTest {

    @TempDir
    Path tmp;

    @Test
    void sameSeedProducesTheSameFile() throws Exception {
        Path a = tmp.resolve("a.csv");
        Path b = tmp.resolve("sotto").resolve("b.csv");                 // la cartella viene creata
        int rows = SalesDataGenerator.generate(a, SalesDataGenerator.SEED);
        SalesDataGenerator.generate(b, SalesDataGenerator.SEED);
        assertEquals(5019, rows);
        assertEquals(-1, Files.mismatch(a, b), "i due file devono coincidere byte per byte");
    }

    @Test
    void reproducesTheVersionedDataFile() throws Exception {
        Path versionato = Path.of("data", "sales.csv");
        assumeTrue(Files.exists(versionato), "data/sales.csv non presente: test saltato");
        Path generato = tmp.resolve("sales.csv");
        SalesDataGenerator.generate(generato, SalesDataGenerator.SEED);
        assertEquals(-1, Files.mismatch(versionato, generato));
    }

    @Test
    void weekendWeighsMore() {
        assertEquals(1.8, SalesDataGenerator.dayFactor(DayOfWeek.SATURDAY));
        assertEquals(0.7, SalesDataGenerator.dayFactor(DayOfWeek.MONDAY));
        assertEquals(1.0, SalesDataGenerator.dayFactor(DayOfWeek.WEDNESDAY));
    }
}
