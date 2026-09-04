package com.arctura.balance_ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps the Balance Ledger Spring application and component scan.
 *
 * <p>This class is intentionally small: Spring Boot discovers controllers,
 * services, repositories, filters, and other components from this package
 * downward.</p>
 */
@SpringBootApplication
public class BalanceLedgerApplication {

	/**
	 * Starts the Balance Ledger application.
	 *
	 * @param args command-line arguments passed through to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(BalanceLedgerApplication.class, args);
	}

}
