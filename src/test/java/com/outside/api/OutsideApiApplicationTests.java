package com.outside.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic smoke test to verify the Spring application context loads successfully.
 *
 * This test makes sure all the beans, configurations, and dependencies are set up
 * correctly. If this fails, it means there's a configuration issue in the app.
 */
@SpringBootTest
class OutsideApiApplicationTests {

	@Test
	void contextLoads() {
		// This test passes if the application context loads without errors
		// No assertions needed - Spring will throw an exception if something is wrong
	}

}
