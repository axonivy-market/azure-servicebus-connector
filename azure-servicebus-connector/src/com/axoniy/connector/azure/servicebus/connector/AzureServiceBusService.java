package com.axoniy.connector.azure.servicebus.connector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.Ivy;

public class AzureServiceBusService {
	private static final AzureServiceBusService INSTANCE = new AzureServiceBusService();
	private static com.azure.messaging.servicebus.ServiceBusSenderClient sender;
	private static final String AZURE_SERVICEBUS_CONNECTOR_GLOBAL_VARIABLE = "azure-servicebus-connector";
	private static final Map<String, ServiceBusSenderClient> senders = new HashMap<>();

	private AzureServiceBusService() {
	}

	public static String getAzureServicebusConnectorGlobalVariableName() {
		return AZURE_SERVICEBUS_CONNECTOR_GLOBAL_VARIABLE;
	}

	public static AzureServiceBusService get() {
		return INSTANCE;
	}

	public synchronized ServiceBusSenderClient sender(String configurationName) {
		var sender = senders.get(configurationName);
		if(sender == null) {
			var props = getConfigurationProperties(configurationName);
			var connString = "xxx";
			var queueName = "yyy";

			Ivy.log().info("Building sender for queue ''{0}''", queueName);
			sender = new ServiceBusClientBuilder()
					.connectionString(connString)
					.sender()
					.queueName(queueName)
					.buildClient();
			senders.put(configurationName, sender);
		}
		return sender;
	}

	/**
	 * Return the configuration properties of a specific configuration stored in global variables.
	 * 
	 * @param configurationName
	 * @return
	 */
	public Properties getConfigurationProperties(String configurationName) {
		var properties = new Properties();
		var seen = new HashSet<String>();

		mergeProperties(configurationName, seen, properties);

		return properties;
	}

	/**
	 * Convert {@link Properties} to {@link String}.
	 * 
	 * @param properties
	 * @return
	 */
	public String getPropertiesString(Properties properties) {
		return properties.entrySet().stream()
				.sorted(Comparator.comparing(e -> e.getKey().toString()))
				.map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
				.collect(Collectors.joining("\n"));
	}

	protected <T> T getVar(String varName, Function<String, T> converter, T defaultValue) {
		var returnValue = defaultValue;
		var value = Ivy.var().get(varName);
		try {
			returnValue = converter.apply(value);
		}
		catch(Exception e) {
			Ivy.log().error("Could not convert global variable ''{0}'' value ''{1}'', using default ''{2}''",
					e, varName, value, defaultValue);
		}

		return returnValue;
	}

	/**
	 * Read properties from global variables.
	 * 
	 * If a configuration contains a value for inherited,
	 * then the inherited configuration will be read first.
	 * 
	 * @param configurationName
	 * @param seen
	 * @param properties
	 */
	protected void mergeProperties(String configurationName, Set<String> seen, Properties properties) {
		if(!seen.add(configurationName)) {
			throw BpmError.create("azure:servicebus:connector:configloop")
			.withMessage("Found configuration loop with already seen configuration '%s".formatted(configurationName))
			.build();
		}
		var whichAbs = "%s.%s.".formatted(getAzureServicebusConnectorGlobalVariableName(), configurationName);

		var inheritedProperties = new Properties();
		var newProperties = new Properties();

		for (var v : Ivy.var().all()) {
			var name = v.name();
			if(name.startsWith(whichAbs)) {
				name = name.substring(whichAbs.length());
				var value = v.value();
				if(name.equals("inherit")) {
					mergeProperties(value, seen, inheritedProperties);
				}
				else {
					newProperties.put(name, value);
				}
			}
		}

		properties.putAll(inheritedProperties);
		properties.putAll(newProperties);
	}

}
