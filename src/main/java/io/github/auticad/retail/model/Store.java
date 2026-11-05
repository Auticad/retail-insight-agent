package io.github.auticad.retail.model;

public class Store {
	String id;
	String city;
	double sqm;
	double dailyRevenue;
	
	Store(String id, String city, double sqm){
		this.id = id;
		this.city = city;
		this.sqm = sqm;
	}
	
	void registreSale(double amount) {
		dailyRevenue += amount;
	}
	
	double revenuePerSqm() {
		return dailyRevenue/sqm;
	}
	
	public static void main(String[] args) {
		Store[] negozi = {
				new Store("S1", "Monza", 180),
	            new Store("S2", "Lissone", 95),
	            new Store("S3", "Desio", 130)
		};
		negozi[0].registreSale(4200);
		negozi[1].registreSale(2600);
		negozi[2].registreSale(2900);
		Store migliore = negozi[0];
		for(Store s : negozi) {
			if (s.revenuePerSqm() > migliore.revenuePerSqm()) {
				migliore = s;
			}
		}
		System.out.printf("%s: %.2f €/mq%n", migliore.city, migliore.revenuePerSqm());

	}

}
