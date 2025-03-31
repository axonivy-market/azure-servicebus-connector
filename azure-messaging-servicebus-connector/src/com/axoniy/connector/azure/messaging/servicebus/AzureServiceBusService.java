package com.axoniy.connector.azure.messaging.servicebus;

import java.time.LocalDateTime;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import ch.ivyteam.ivy.environment.Ivy;

public class AzureServiceBusService {
	private static final AzureServiceBusService INSTANCE = new AzureServiceBusService();
	private static ServiceBusSenderClient sender;

	public static AzureServiceBusService get() {
		return INSTANCE;
	}

	public ServiceBusSenderClient client() {
		if(sender == null) {
			var connString = "xxx";
			var queueName = "yyy";

			Ivy.log().info("Building sender for queue ''{0}''", queueName);
			sender = new ServiceBusClientBuilder()
					.connectionString(connString)
					.sender()
					.queueName(queueName)
					.buildClient();
		}
		return sender;
	}

	public void test() {
		String testMsg = "The current time is %s".formatted(LocalDateTime.now());
		Ivy.log().info("Sending text as message: ''{0}''", testMsg);

		var msg = new ServiceBusMessage(testMsg);

		client().sendMessage(msg);
		Ivy.log().info("Sent message via client: ''{0}''.", sender);
	}
}
