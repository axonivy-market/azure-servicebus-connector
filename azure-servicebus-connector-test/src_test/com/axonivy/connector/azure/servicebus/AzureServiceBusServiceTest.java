package com.axonivy.connector.azure.servicebus;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.axonivy.connector.azure.servicebus.AzureServiceBusService.Configuration;
import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;

import ch.ivyteam.ivy.environment.IvyTest;

import static org.assertj.core.api.Assertions.assertThat;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Condition.*;

@IvyTest
@IvyWebTest(headless = false)
@Testcontainers
public class AzureServiceBusServiceTest {

	private static final String DOCKER_FILE_PATH = "src_test/com/axonivy/connector/azure/servicebus/docker-config/docker-compose-default.yml";
	private static final String DEMO_PROCESS_PATH = "/azure-servicebus-connector-demo/195F0CC16F9EFA3C/start.ivp";
	private static final String FORM_SEND_MESSAGE_ID = "form:send";
	private static final String FORM_SEND_BUTTON_ID = "form:send-button";
	private static final String FROM_MESSAGE_ID = "form:message";

	static DockerComposeContainer<?> environment;

	@BeforeAll
	@SuppressWarnings("resource")
	static void setupDocker() throws IOException {
		environment = new DockerComposeContainer<>(new File(DOCKER_FILE_PATH)).withExposedService("emulator", 5672);
		environment.start();
	}

	@AfterAll
	static void stopDocker() {
		environment.stop();
	}

	@Test
	public void test_runningWithMockAzureServiceBusSimulator() {
		open(EngineUrl.createProcessUrl(DEMO_PROCESS_PATH));
		$(By.id(FORM_SEND_MESSAGE_ID)).click();
		$(By.id(FORM_SEND_MESSAGE_ID)).clear();
		$(By.id(FORM_SEND_MESSAGE_ID)).sendKeys("send message");
		$(By.id(FORM_SEND_BUTTON_ID)).click();
		$(By.id(FROM_MESSAGE_ID)).shouldHave(text("Send message"), Duration.ofSeconds(40));

		$(By.id(FORM_SEND_MESSAGE_ID)).click();
		$(By.id(FORM_SEND_MESSAGE_ID)).clear();
		$(By.id(FORM_SEND_MESSAGE_ID)).sendKeys("Send message2");
		$(By.id(FORM_SEND_BUTTON_ID)).click();

		String expectedValueAfterSending = """
				Sent a message to 'queue1':
				send message
				Sent a message to 'queue1':
				Send message2
				 """;
		$(By.id(FROM_MESSAGE_ID)).shouldHave(text(expectedValueAfterSending), Duration.ofSeconds(40));
	}

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
