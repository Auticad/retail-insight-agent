package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Store;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WeeklyReportTest {

    private static final LocalDate LUNEDI = LocalDate.of(2026, 10, 5);

    @Test
    void rowsAreSortedByRevenueThenName() {
        List<WeeklyReport.Row> rows = WeeklyReport.rows(TestData.sales(), TestData.stores(), LUNEDI);
        assertEquals(List.of("Monza Centro", "Lissone", "Desio"), rows.stream().map(r -> r.store().getName()).toList());

        WeeklyReport.Row monza = rows.get(0);
        assertEquals(2, monza.piecesOn(DayOfWeek.MONDAY));
        assertEquals(4, monza.piecesOn(DayOfWeek.TUESDAY));
        assertEquals(0, monza.piecesOn(DayOfWeek.SUNDAY));
        assertEquals(6, monza.totalPieces());
        assertEquals(0, new BigDecimal("140.00").compareTo(monza.revenue()));
    }

    @Test
    void comparatorBreaksTiesByName() {
        WeeklyReport.Row zeta = row("Zeta", "100");
        WeeklyReport.Row alfa = row("Alfa", "100");
        WeeklyReport.Row top = row("Media", "200");
        List<WeeklyReport.Row> rows = new ArrayList<>(List.of(zeta, alfa, top));
        rows.sort(WeeklyReport.ORDINE);
        assertEquals(List.of(top, alfa, zeta), rows);
    }

    @Test
    void reportNamesBestDayAndBestStore() {
        String report = WeeklyReport.genera(TestData.sales(), TestData.stores(), LUNEDI);
        assertTrue(report.contains("Settimana dal 2026-10-05 al 2026-10-11"));
        assertTrue(report.contains("Giorno migliore della catena: mar (4 pezzi)"), report);
        assertTrue(report.contains("Negozio migliore: Monza Centro, Monza (140,00 €)"), report);
    }

    @Test
    void emptyWeekIsReportedExplicitly() {
        String report = WeeklyReport.genera(TestData.sales(), TestData.stores(), LocalDate.of(2027, 1, 4));
        assertTrue(report.contains("Nessuna vendita in questa settimana"));
    }

    @Test
    void lastFullWeekEndsOnLastSunday() {
        // ultima vendita mercoledì 4 novembre: l'ultima settimana completa va dal 26 ottobre al 1 novembre
        assertEquals(LocalDate.of(2026, 10, 26), WeeklyReport.lastFullWeekMonday(TestData.sales()).orElseThrow());
        assertTrue(WeeklyReport.lastFullWeekMonday(List.of()).isEmpty());
    }

    @Test
    void weekMustStartOnMonday() {
        assertThrows(IllegalArgumentException.class,
                () -> WeeklyReport.rows(TestData.sales(), TestData.stores(), LUNEDI.plusDays(1)));
    }

    private static WeeklyReport.Row row(String name, String revenue) {
        return new WeeklyReport.Row(new Store(name, name, "Monza", 100), Map.of(), 0, new BigDecimal(revenue));
    }
}
