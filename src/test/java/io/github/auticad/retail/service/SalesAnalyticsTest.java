package io.github.auticad.retail.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static io.github.auticad.retail.service.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

class SalesAnalyticsTest {

    private final SalesAnalytics analytics = TestData.analytics();

    @Test
    void unknownStoresAndSkusAreReportedAndExcluded() {
        assertEquals(Set.of("S9/XX-01"), analytics.anomalies());
        assertEquals(5, analytics.size());
        assertEquals(LocalDate.of(2026, 10, 5), analytics.firstDate().orElseThrow());
        assertEquals(LocalDate.of(2026, 11, 4), analytics.lastDate().orElseThrow());
    }

    @Test
    void revenueByStoreIncludesStoresWithoutSales() {
        List<StoreRevenue> rows = analytics.revenueByStore(OTT_1, NOV_30);
        assertEquals(List.of("S1", "S2", "S3"), rows.stream().map(StoreRevenue::storeId).toList());

        StoreRevenue s1 = rows.get(0);
        assertMoney("180.00", s1.revenue());
        assertMoney("112.00", s1.margin());
        assertEquals(new BigDecimal("62.22"), s1.marginPct());
        assertEquals(new BigDecimal("1.80"), s1.revenuePerSqm());

        StoreRevenue s2 = rows.get(1);
        assertMoney("111.00", s2.revenue());
        assertEquals(new BigDecimal("58.56"), s2.marginPct());

        StoreRevenue s3 = rows.get(2);
        assertEquals(0, s3.revenue().signum());
        assertEquals(new BigDecimal("0.00"), s3.marginPct(), "nessuna divisione per zero");
    }

    @Test
    void periodBoundsAreInclusive() {
        List<StoreRevenue> rows = analytics.revenueByStore(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 5));
        assertMoney("80.00", rows.get(0).revenue());
    }

    @Test
    void topProductsByEachMetric() {
        List<ProductRank> revenue = analytics.topProducts(OTT_1, NOV_30, RankingMetric.REVENUE, 3);
        assertEquals(List.of("FE-01", "SC-07", "TS-03"), revenue.stream().map(ProductRank::sku).toList());
        assertMoney("156.00", revenue.get(0).value());
        assertEquals("Felpa", revenue.get(0).name());

        List<ProductRank> margin = analytics.topProducts(OTT_1, NOV_30, RankingMetric.MARGIN, 1);
        assertEquals("FE-01", margin.get(0).sku());
        assertMoney("92.00", margin.get(0).value());

        // FE-01 e TS-03 hanno entrambi 4 pezzi: a parità decide lo SKU
        List<ProductRank> qty = analytics.topProducts(OTT_1, NOV_30, RankingMetric.QUANTITY, 2);
        assertEquals(List.of("FE-01", "TS-03"), qty.stream().map(ProductRank::sku).toList());
        assertMoney("4", qty.get(1).value());
    }

    @Test
    void marginByCategorySortedByMarginPercent() {
        List<CategoryKpi> kpi = analytics.marginByCategory(OTT_1, NOV_30);
        assertEquals(List.of("basic", "accessori", "maglieria"), kpi.stream().map(CategoryKpi::category).toList());
        assertEquals(new BigDecimal("66.67"), kpi.get(0).marginPct());
        assertEquals(new BigDecimal("60.00"), kpi.get(1).marginPct());
        CategoryKpi maglieria = kpi.get(2);
        assertMoney("156.00", maglieria.revenue());
        assertMoney("92.00", maglieria.margin());
        assertEquals(new BigDecimal("58.97"), maglieria.marginPct());
        assertEquals(1, maglieria.skus());
    }

    @Test
    void comparePeriodsPutsBiggestDropFirst() {
        List<PeriodComparison> cmp = analytics.comparePeriods(OTT_1, OTT_31, NOV_1, NOV_30);
        assertEquals(List.of("S1", "S3", "S2"), cmp.stream().map(PeriodComparison::storeId).toList());
        assertEquals(new BigDecimal("-71.43"), cmp.get(0).changePct());    // da 140 a 40
        assertEquals(new BigDecimal("0.00"), cmp.get(1).changePct());      // nessuna vendita in entrambi
        assertEquals(new BigDecimal("108.33"), cmp.get(2).changePct());    // da 36 a 75
    }

    @Test
    void invalidArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> analytics.revenueByStore(NOV_30, OTT_1));
        assertThrows(IllegalArgumentException.class, () -> analytics.topProducts(OTT_1, NOV_30, RankingMetric.REVENUE, 0));
        assertThrows(NullPointerException.class, () -> analytics.topProducts(OTT_1, NOV_30, null, 3));
    }

    @Test
    void resultsAreImmutable() {
        List<StoreRevenue> rows = analytics.revenueByStore(OTT_1, NOV_30);
        assertThrows(UnsupportedOperationException.class, () -> rows.remove(0));
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "atteso " + expected + ", trovato " + actual);
    }
}
