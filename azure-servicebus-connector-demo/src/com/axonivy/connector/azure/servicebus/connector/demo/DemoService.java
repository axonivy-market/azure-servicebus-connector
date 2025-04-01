package com.axonivy.connector.azure.servicebus.connector.demo;

import java.time.LocalDateTime;

import com.axoniy.connector.azure.servicebus.connector.AzureServiceBusService;
import com.azure.messaging.servicebus.ServiceBusMessage;

import ch.ivyteam.ivy.environment.Ivy;

public class DemoService {
	private static DemoService INSTANCE = new DemoService();

	public static DemoService get() {
		return INSTANCE;
	}

	public void test() {
		String testMsg = "The current time is %s".formatted(LocalDateTime.now());
		Ivy.log().info("Sending text as message: ''{0}''", testMsg);

		var msg = new ServiceBusMessage(testMsg);

		AzureServiceBusService.get();

		//		client().sendMessage(msg);
		//		Ivy.log().info("Sent message via client: ''{0}''.", sender);
	}

}
