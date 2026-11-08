package io.github.auticad.retail.source;

import io.github.auticad.retail.model.PerishableProduct;
import io.github.auticad.retail.model.Product;
import io.github.auticad.retail.model.Store;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dati di prova usati dal menu finché non arrivano CSV e database.
 * Negozi e prodotti di abbigliamento sono gli stessi di db/init/02_seed.sql;
 * i tre prodotti alimentari esistono solo qui, per mostrare il prezzo dinamico.
 * Ogni metodo crea oggetti nuovi: chi li modifica (sell, restock) non altera le chiamate successive.
 */
public final class DemoData {

    private DemoData() {
    }

    public static Store[] stores() {
        return new Store[] {
            new Store("S1", "Monza Centro", "Monza", 180),
            new Store("S2", "Lissone", "Lissone", 95),
            new Store("S3", "Desio", "Desio", 130)
        };
    }

    /** Pezzi venduti nella settimana di prova: una riga per negozio di stores(), una colonna per giorno. */
    public static int[][] weeklyPieces() {
        return new int[][] {
            {120, 98, 110, 130, 160, 210, 190},
            { 80, 75,  90,  85, 120, 170, 150},
            { 60, 55,  70,  65,  90, 140,   0}
        };
    }

    /**
     * Catalogo con scorte. Le scadenze dei deperibili sono relative alla data di riferimento,
     * così ogni scaglione di prezzo è visibile qualunque giorno si esegua il programma.
     */
    public static Product[] products(LocalDate reference) {
        return new Product[] {
            product("FE-01", "Felpa in cotone", "maglieria", "39.90", "16.00", 140),
            product("JE-02", "Jeans slim", "denim", "59.00", "27.50", 220),
            product("TS-03", "T-shirt basic", "basic", "14.90", "5.20", 380),
            product("MA-05", "Maglione merino", "maglieria", "79.00", "38.00", 60),
            product("GI-06", "Giacca imbottita", "capispalla", "129.00", "61.00", 45),
            product("SC-07", "Sciarpa in lana", "accessori", "24.90", "9.80", 90),
            product("CA-08", "Camicia oxford", "camiceria", "34.90", "14.50", 110),
            product("PO-09", "Polo piquet", "basic", "29.90", "11.90", 150),
            product("CR-10", "Cintura in pelle", "accessori", "19.90", "7.10", 75),
            product("PA-11", "Pantalone chino", "pantaloni", "49.90", "21.00", 130),
            product("CI-12", "Cuffia in lana", "accessori", "22.90", "8.40", 85),
            product("BO-13", "Bomber leggero", "capispalla", "69.00", "30.50", 40),
            perishable("CO-14", "Cioccolatini assortiti", "12.90", "5.40", 30, reference.plusDays(1)),
            perishable("TE-15", "Tè in foglie", "8.90", "3.10", 25, reference.plusDays(3)),
            perishable("BI-16", "Biscotti al burro", "6.50", "2.30", 40, reference.plusDays(20))
        };
    }

    private static Product product(String sku, String name, String category, String price, String cost, int stock) {
        return new Product(sku, name, category, new BigDecimal(price), new BigDecimal(cost), stock);
    }

    private static PerishableProduct perishable(String sku, String name, String price, String cost,
                                                int stock, LocalDate expiry) {
        return new PerishableProduct(sku, name, "alimentari", new BigDecimal(price), new BigDecimal(cost), stock, expiry);
    }
}
