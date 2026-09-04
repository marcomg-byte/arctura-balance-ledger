package com.arctura.payment_bridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke tests verifying that the Spring application context starts with the
 * configured beans.
 *
 * <p>This test guards the top-level Spring Boot configuration and component
 * scanning setup.</p>
 */
@SpringBootTest
class PaymentBridgeApplicationTests {

	/**
	 * Verifies that the application context can be created successfully.
	 */
	@Test
	void contextLoads() {
	}

}
