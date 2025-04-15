package com.axonivy.connector.azure.servicebus.connector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.azure.servicebus.connector.AzureServiceBusService.Configuration;

import ch.ivyteam.ivy.environment.IvyTest;

@IvyTest
public class AzureServiceBusServiceTest {

	@Test
	public void testExampleConfig() {
		var conf = Configuration.fromGlobalVariables("example");
		assertThat(conf).isNotNull();

		var nonExist = Configuration.fromGlobalVariables("foobar");
		assertThat(nonExist).isNotNull();
		assertThat(nonExist.connectionString()).isNull();
		assertThat(nonExist.queueName()).isNull();
		assertThat(nonExist.topicName()).isNull();
	}
}
