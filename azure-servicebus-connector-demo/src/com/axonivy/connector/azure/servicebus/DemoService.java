package com.axonivy.connector.azure.servicebus;

public class DemoService {
	private static String DEMO_CONFIG = "demo";
	private static DemoService INSTANCE = new DemoService();

	public static DemoService get() {
		return INSTANCE;
	}

	public String getConfigName() {
		return DEMO_CONFIG;
	}
}
