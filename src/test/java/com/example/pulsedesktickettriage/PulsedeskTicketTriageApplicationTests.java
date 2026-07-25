package com.example.pulsedesktickettriage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "huggingface.api.token=dummy-token-for-tests")
class PulsedeskTicketTriageApplicationTests {

	@Test
	void contextLoads() {
	}

}
