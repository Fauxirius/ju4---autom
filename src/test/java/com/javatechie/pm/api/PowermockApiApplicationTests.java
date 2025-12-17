package com.javatechie.pm.api;

import com.javatechie.pm.api.dto.OrderRequest;
import com.javatechie.pm.api.service.OrderService;
import com.javatechie.pm.api.util.NotificationUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class PowermockApiApplicationTests {

	@Autowired
	private OrderService service;

	@MockBean
	private NotificationUtil notificationUtil;

	@Test
	void testCheckoutOrder_shouldSendEmail() {
		// Given
		OrderRequest request = new OrderRequest(111, "Mobile", 1, 10000, "test@gmail.com", true);

		// When
		service.checkoutOrder(request);

		// Then
		verify(notificationUtil, times(1)).sendEmail(request.getEmailId());
	}
}
