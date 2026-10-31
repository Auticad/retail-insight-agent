package io.github.auticad.retail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("=== Retail Insight Agent v0.1 ===");
        System.out.println("KPI di un prodotto (decimali con virgola o punto)");

        // Lettura di righe intere: evita la trappola nextInt() + nextLine()
        System.out.print("Prezzo di vendita (€): ");
        BigDecimal prezzo = new BigDecimal(in.nextLine().strip().replace(',', '.'));

        System.out.print("Costo di acquisto (€): ");
        BigDecimal costo = new BigDecimal(in.nextLine().strip().replace(',', '.'));

        System.out.print("Quantità venduta: ");
        int quantita = Integer.parseInt(in.nextLine().strip());
        
        in.close();
        
        // Calcoli con BigDecimal: importi esatti al centesimo
        BigDecimal q = BigDecimal.valueOf(quantita);
        BigDecimal fatturato = prezzo.multiply(q);
        BigDecimal margine = prezzo.subtract(costo).multiply(q);
        BigDecimal marginePct = prezzo.subtract(costo)
                .multiply(BigDecimal.valueOf(100))
                .divide(prezzo, 2, RoundingMode.HALF_UP);   // ArithmeticException se il prezzo è 0

        // Output in formato italiano: 1.234,56 € (giorno 3)
        System.out.println();
        System.out.println(String.format(Locale.ITALY, "Fatturato:  %,12.2f €", fatturato));
        System.out.println(String.format(Locale.ITALY, "Margine:    %,12.2f €", margine));
        System.out.println(String.format(Locale.ITALY, "Margine %%: %12.2f %%", marginePct));
    }
}
