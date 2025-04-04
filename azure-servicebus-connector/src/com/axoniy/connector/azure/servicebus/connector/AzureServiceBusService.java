package com.axoniy.connector.azure.servicebus.connector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.Ivy;

public class AzureServiceBusService {
	private static final AzureServiceBusService INSTANCE = new AzureServiceBusService();
	private static final Map<String, ServiceBusSenderClient> senders = new HashMap<>();
	private static final Map<String, ServiceBusReceiverClient> receivers = new HashMap<>();
	private static final GlobalVariablesSupplier GLOBAL_VARIABLES_SUPPLIER = new GlobalVariablesSupplier();

	private AzureServiceBusService() {
	}

	public static AzureServiceBusService get() {
		return INSTANCE;
	}

	/**
	 * Create and cache a sender configured by global variables.
	 * 
	 * @param configurationName
	 * @return
	 */
	public ServiceBusSenderClient sender(String configurationName) {
		return sender(configurationName, GLOBAL_VARIABLES_SUPPLIER);
	}

	/**
	 * Create and cache a sender.
	 * 
	 * @param configurationName
	 * @param senderSupplier
	 * @return
	 */
	public synchronized ServiceBusSenderClient sender(String configurationName, AzureServiceBusSenderSupplier senderSupplier) {
		var sender = senders.get(configurationName);
		if(sender == null) {
			sender = senderSupplier.supplySender(configurationName);
			senders.put(configurationName, sender);
		}
		return sender;

	}

	/**
	 * Create and cache a receiver configured by global variables.
	 * 
	 * @param configurationName
	 * @return
	 */
	public ServiceBusReceiverClient receiver(String configurationName) {
		return receiver(configurationName, GLOBAL_VARIABLES_SUPPLIER);
	}


	/**
	 * Create and cache a receiver.
	 * 
	 * @param configurationName
	 * @param receiverSupplier
	 * @return
	 */
	public synchronized ServiceBusReceiverClient receiver(String configurationName, AzureServiceBusReceiverSupplier receiverSupplier) {
		var receiver = receivers.get(configurationName);
		if(receiver == null) {
			receiver = receiverSupplier.supplyReceiver(configurationName);
			receivers.put(configurationName, receiver);
		}
		return receiver;
	}

	/**
	 * Create a processor from global variables.
	 * 
	 * @param configurationName
	 * @param processMessage
	 * @param processError
	 * @return
	 */
	public ServiceBusProcessorClient processor(String configurationName, Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError) {
		Ivy.log().info("Building processor for configuration ''{0}''.", configurationName);
		var conf = Configuration.fromGlobalVariables(configurationName);

		var builder = createBuilder(conf)
				.processor();

		if(isSet(conf.queueName())) {
			builder = builder.queueName(conf.queueName());
		}

		if(isSet(conf.topicName())) {
			builder = builder.topicName(conf.topicName());
		}

		if(isSet(conf.subscriptionName())) {
			builder = builder.subscriptionName(conf.subscriptionName());
		}

		var processor = builder
				.processMessage(processMessage)
				.processError(processError)
				.buildProcessorClient();

		return processor;
	}

	public static ServiceBusClientBuilder createBuilder(Configuration conf) {
		var builder = new ServiceBusClientBuilder();

		if(isSet(conf.connectionString())) {
			builder = builder.connectionString(conf.connectionString());
		}

		if(isSet(conf.fullyQualifiedNamespace())) {
			builder = builder.fullyQualifiedNamespace(conf.fullyQualifiedNamespace())
					.credential(new DefaultAzureCredentialBuilder().build());
		}

		return builder;
	}

	protected static boolean isSet(String value) {
		return StringUtils.isNotBlank(value);
	}

	protected static class GlobalVariablesSupplier implements AzureServiceBusSenderSupplier, AzureServiceBusReceiverSupplier {
		/**
		 * Supply a sender by configuration from global variables.
		 * 
		 * @param configurationName
		 * @return
		 */
		@Override
		public ServiceBusSenderClient supplySender(String configurationName) {
			Ivy.log().info("Building sender for configuration ''{0}''.", configurationName);
			var conf = Configuration.fromGlobalVariables(configurationName);

			var builder = createBuilder(conf)
					.sender();

			if(isSet(conf.queueName())) {
				builder = builder.queueName(conf.queueName());
			}

			if(isSet(conf.topicName())) {
				builder = builder.topicName(conf.topicName());
			}

			var sender = builder
					.buildClient();

			return sender;
		}

		/**
		 * Supply a receiver by configuration from global variables.
		 * 
		 * @param configurationName
		 * @return
		 */
		@Override
		public ServiceBusReceiverClient supplyReceiver(String configurationName) {
			Ivy.log().info("Building receiver for configuration ''{0}''.", configurationName);
			var conf = Configuration.fromGlobalVariables(configurationName);

			var builder = createBuilder(conf)
					.receiver();

			if(isSet(conf.queueName())) {
				builder = builder.queueName(conf.queueName());
			}

			if(isSet(conf.topicName())) {
				builder = builder.topicName(conf.topicName());
			}

			if(isSet(conf.subscriptionName())) {
				builder = builder.subscriptionName(conf.subscriptionName());
			}

			var receiver = builder
					.buildClient();
			return receiver;
		}


	}


	protected record Configuration(String fullyQualifiedNamespace, String connectionString, String queueName, String topicName, String subscriptionName) {
		private static final String AZURE_SERVICEBUS_GLOBAL_VARIABLE = "azure-servicebus-connector";
		private static final String AZURE_SERVICEBUS_CONNECTION_STRING_GLOBAL_VARIABLE = "connectionString";
		private static final String AZURE_SERVICEBUS_FULLY_QUALIFIED_NAMESPACE_GLOBAL_VARIABLE = "fullyQualifiedNamespace";
		private static final String AZURE_SERVICEBUS_QUEUE_NAME_GLOBAL_VARIABLE = "queueName";
		private static final String AZURE_SERVICEBUS_TOPIC_NAME_GLOBAL_VARIABLE = "topicName";
		private static final String AZURE_SERVICEBUS_SUBSCRIPTION_NAME_GLOBAL_VARIABLE = "subscriptionName";

		public static Configuration fromGlobalVariables(String configurationName) {
			var props = getConfigurationProperties(configurationName);

			return new Configuration(
					props.getProperty(AZURE_SERVICEBUS_FULLY_QUALIFIED_NAMESPACE_GLOBAL_VARIABLE),
					props.getProperty(AZURE_SERVICEBUS_CONNECTION_STRING_GLOBAL_VARIABLE),
					props.getProperty(AZURE_SERVICEBUS_QUEUE_NAME_GLOBAL_VARIABLE),
					props.getProperty(AZURE_SERVICEBUS_TOPIC_NAME_GLOBAL_VARIABLE),
					props.getProperty(AZURE_SERVICEBUS_SUBSCRIPTION_NAME_GLOBAL_VARIABLE));
		}

		/**
		 * Get the base of the configuration.
		 * 
		 * @return
		 */
		public static String getAzureServicebusGlobalVariable() {
			return AZURE_SERVICEBUS_GLOBAL_VARIABLE;
		}

		/**
		 * Return the configuration properties of a specific configuration stored in global variables.
		 * 
		 * @param configurationName
		 * @return
		 */
		public static Properties getConfigurationProperties(String configurationName) {
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
		protected static void mergeProperties(String configurationName, Set<String> seen, Properties properties) {
			if(!seen.add(configurationName)) {
				throw BpmError.create("azure:servicebus:connector:configloop")
				.withMessage("Found configuration loop with already seen configuration '%s".formatted(configurationName))
				.build();
			}
			var whichAbs = "%s.%s.".formatted(AZURE_SERVICEBUS_GLOBAL_VARIABLE, configurationName);

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
}
