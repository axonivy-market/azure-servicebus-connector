package com.axonivy.connector.azure.servicebus.connector.demo;

import java.time.Duration;
import java.time.LocalDateTime;

import com.axoniy.connector.azure.servicebus.connector.AzureServiceBusService;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

import ch.ivyteam.ivy.environment.Ivy;

public class DemoService {
	private static String DEMO_CONFIG = "demo";
	private static DemoService INSTANCE = new DemoService();

	public static DemoService get() {
		return INSTANCE;
	}

	public void sendTest() {
		String testMsg = "The current time is %s".formatted(LocalDateTime.now());
		Ivy.log().info("Sending text as message: ''{0}''", testMsg);

		var sender = AzureServiceBusService.get().sender(DEMO_CONFIG);

		var msg = new ServiceBusMessage(testMsg);
		sender.sendMessage(msg);
		Ivy.log().info("Sent message via client: ''{0}''.", sender);
	}

	public void receiveTest() {
		Ivy.log().info("Receiving message");

		var receiver = AzureServiceBusService.get().receiver(DEMO_CONFIG);

		var messages = receiver.receiveMessages(10, Duration.ofSeconds(5));

		var rcvd = 0;

		for (ServiceBusReceivedMessage message : messages) {
			Ivy.log().info("Received message: {0}", message.getBody());
			rcvd++;
		}


		Ivy.log().info("Received {0} messages.", rcvd);
	}

}
