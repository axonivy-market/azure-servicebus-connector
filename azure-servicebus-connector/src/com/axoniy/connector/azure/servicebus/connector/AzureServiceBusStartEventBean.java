/**
 * 
 */
package com.axoniy.connector.azure.servicebus.connector;

import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;

import ch.ivyteam.ivy.process.eventstart.AbstractProcessStartEventBean;
import ch.ivyteam.ivy.process.eventstart.IProcessStartEventBean;
import ch.ivyteam.ivy.process.eventstart.IProcessStartEventBeanRuntime;
import ch.ivyteam.ivy.process.extension.ProgramConfig;
import ch.ivyteam.ivy.process.extension.ui.ExtensionUiBuilder;
import ch.ivyteam.ivy.process.extension.ui.UiEditorExtension;
import ch.ivyteam.ivy.request.RequestException;
import ch.ivyteam.ivy.service.ServiceException;
import ch.ivyteam.log.Logger;

/**
 * {@link IProcessStartEventBean} to listen on the Azure Service Bus.
 * 
 * You may override this class to supply your own processor.
 */
public class AzureServiceBusStartEventBean extends AbstractProcessStartEventBean {
	private static final String AZURE_SERVICEBUS_CONFIGURATION_NAME_FIELD = "azureServiceBusConfigurationNameField";
	private ServiceBusProcessorClient processor = null;

	/**
	 * Constructor.
	 */
	public AzureServiceBusStartEventBean() {
		super("AzureServiceBusStartEventBean", "Listen on the Azure Service Bus");
	}

	@Override
	public void initialize(IProcessStartEventBeanRuntime eventRuntime, ProgramConfig programConfig) {
		super.initialize(eventRuntime, programConfig);
		eventRuntime.poll().disable();
	}

	@Override
	public void start(IProgressMonitor monitor) throws ServiceException {
		var configurationName = getAzureServiceBusConfigurationName();

		log().debug("Starting Azure Service Bus processor for configuration name: ''{0}''", configurationName);

		processor = createProcessor(configurationName);
		processor.start();

		super.start(monitor);
		log().info("Started");
	}

	/**
	 * Override this function to create a {@link ServiceBusProcessorClient} with special settings not supported in global variables.
	 * 
	 * @param configurationName
	 * @return
	 */
	protected ServiceBusProcessorClient createProcessor(String configurationName) {
		return AzureServiceBusService.get().processor(configurationName, this::processMessage, this::processError);
	}

	protected void processMessage(ServiceBusReceivedMessageContext messageContext) {
		startProcess(messageContext, null);
	}

	protected void processError(ServiceBusErrorContext errorContext) {
		startProcess(null, errorContext);
	}

	protected void startProcess(ServiceBusReceivedMessageContext messageContext, ServiceBusErrorContext errorContext) {
		var type = errorContext != null ? "error" : "message";
		log().debug("Firing task to handle Azure Service Bus message ''{0}'' error ''{1}''.", messageContext, errorContext);
		var processStarter = getEventBeanRuntime()
				.processStarter()
				.withReason("Received Azure Service Bus %s".formatted(type))
				.withParameter("messageContext", messageContext)
				.withParameter("errorContext", errorContext);
		try {
			var eventResponse = processStarter.start();
			log().debug("Azure Service Bus {0} was handled by task {1} and returned with parameters: {2}.",
					type,
					eventResponse.getStartedTask().getId(),
					eventResponse.getParameters().keySet().stream().sorted().collect(Collectors.joining(", ")));
		} catch (RequestException e) {
			log().error("Azure Service Bus %s caused error.".formatted(type), e);
		}
	}

	@Override
	public void stop(IProgressMonitor monitor) throws ServiceException {
		processor.close();
		super.stop(monitor);
	}

	@Override
	public void poll() {
		log().warn("Did not expect call to poll (polling was disabled).");
	}

	/**
	 * 
	 */
	public static class Editor extends UiEditorExtension {

		@Override
		public void initUiFields(ExtensionUiBuilder ui) {
			ui.label("Configuration Name:").create();
			ui.textField(AZURE_SERVICEBUS_CONFIGURATION_NAME_FIELD).create();
			String helpTopic = String.format("""
					Configuration name:
					Name of a collection of global variables below
					%s which defines a specific Kafka consumer configuration.
					""", AzureServiceBusService.Configuration.getAzureServicebusGlobalVariable());
			ui.label(helpTopic).multiline().create();
		}
	}

	protected String getAzureServiceBusConfigurationName() {
		return getConfig().get(AZURE_SERVICEBUS_CONFIGURATION_NAME_FIELD);
	}

	protected Logger log() {
		return getEventBeanRuntime().getRuntimeLogLogger();
	}
}
