package com.arctura.payment_bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps the Payment Bridge Spring application and component scan.
 *
 * <p>This class is intentionally small: Spring Boot discovers controllers,
 * services, repositories, filters, and other components from this package
 * downward.</p>
 */
@SpringBootApplication
public class PaymentBridgeApplication {

	/**
	 * Starts the Payment Bridge application.
	 *
	 * @param args command-line arguments passed through to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(PaymentBridgeApplication.class, args);
	}

}
