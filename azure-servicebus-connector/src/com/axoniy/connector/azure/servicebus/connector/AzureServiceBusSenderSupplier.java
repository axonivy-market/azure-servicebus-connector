package com.axoniy.connector.azure.servicebus.connector;

import com.azure.messaging.servicebus.ServiceBusSenderClient;

/**
 * Implement this interface to supply your own sender if you need more configuration possibilities than the provided supplier. 
 */
public interface AzureServiceBusSenderSupplier {
	/**
	 * Create a sender client.
	 * 
	 * @param configurationName
	 * @return
	 */
	public ServiceBusSenderClient supplySender(String configurationName);
}
