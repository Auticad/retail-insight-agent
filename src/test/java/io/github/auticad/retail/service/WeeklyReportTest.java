package io.github.auticad.retail.service;

import io.github.auticad.retail.model.Store;
import io.github.auticad.retail.source.DemoData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeeklyReportTest {

    private final int[][] pezzi = DemoData.weeklyPieces();

    @Test
    void totalsPerStoreAndPerDay() {
        assertArrayEquals(new int[] {1018, 770, 480}, WeeklyReport.totaliPerNegozio(pezzi));
        assertArrayEquals(new int[] {260, 228, 270, 280, 370, 520, 340}, WeeklyReport.totaliPerGiorno(pezzi));
    }

    @Test
    void maxIndexReturnsFirstOfEqualValues() {
        assertEquals(1, WeeklyReport.indiceMassimo(new int[] {3, 9, 9, 1}));
    }

    @Test
    void reportNamesBestDayAndBestStore() {
        String report = WeeklyReport.genera(DemoData.stores(), pezzi);
        assertTrue(report.contains("Giorno migliore della catena: sab (520 pezzi)"));
        assertTrue(report.contains("Negozio migliore: Monza Centro, Monza (1018 pezzi)"));
    }

    @Test
    void mismatchedInputIsRejected() {
        Store[] dueNegozi = {DemoData.stores()[0], DemoData.stores()[1]};
        assertThrows(IllegalArgumentException.class, () -> WeeklyReport.genera(dueNegozi, pezzi));
    }
}
