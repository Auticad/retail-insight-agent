package io.github.auticad.retail.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleIOTest {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    private ConsoleIO console(String input) {
        return new ConsoleIO(new Scanner(input), new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    void readIntSkipsTextAndOutOfRangeValues() {
        int value = console("abc\n0\n42\n").readInt("Quantità: ", 1, 100);
        assertEquals(42, value);
        assertTrue(output().contains("'abc' non è un numero intero."));
        assertTrue(output().contains("Inserisci un valore tra 1 e 100."));
    }

    @Test
    void readDecimalAcceptsCommaAndRejectsNegatives() {
        ConsoleIO io = console("dieci\n-5\n24,90\n7.5\n");
        assertEquals(new BigDecimal("24.90"), io.readDecimal("Importo: "));
        assertEquals(new BigDecimal("7.5"), io.readDecimal("Importo: "));
        assertTrue(output().contains("non è un importo valido"));
        assertTrue(output().contains("non può essere negativo"));
    }

    @Test
    void readNonBlankSkipsEmptyLinesAndStrips() {
        assertEquals("FE-01", console("\n   \n  FE-01  \n").readNonBlank("SKU: "));
    }

    @Test
    void readDateUsesDefaultOnEmptyLine() {
        LocalDate def = LocalDate.of(2026, 11, 1);
        ConsoleIO io = console("\n2026-13-01\n2026-10-15\n");
        assertEquals(def, io.readDate("Dal", def));
        assertEquals(LocalDate.of(2026, 10, 15), io.readDate("Dal", def));
        assertTrue(output().contains("non è una data valida"));
    }

    @Test
    void readYesNo() {
        ConsoleIO io = console("forse\nSì\nn\n");
        assertTrue(io.readYesNo("Confermi"));
        assertFalse(io.readYesNo("Confermi"));
    }

    @Test
    void endOfInputIsNotRetriedForever() {
        assertThrows(NoSuchElementException.class, () -> console("abc\n").readInt("Numero: ", 1, 10));
    }
}
