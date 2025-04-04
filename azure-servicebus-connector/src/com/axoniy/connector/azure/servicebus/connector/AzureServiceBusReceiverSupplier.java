package com.axoniy.connector.azure.servicebus.connector;

import com.azure.messaging.servicebus.ServiceBusReceiverClient;

/**
 * Implement this interface to supply your own receiver if you need more configuration possibilities than the provided supplier.
 */
public interface AzureServiceBusReceiverSupplier {
	/**
	 * Create a receiver client.
	 * 
	 * @param configurationName
	 * @return
	 */
	public ServiceBusReceiverClient supplyReceiver(String configurationName);
}
