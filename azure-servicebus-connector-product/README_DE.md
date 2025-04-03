# Azure Service Bus Connector

Verbindung zum [Azure Service Bus](https://azure.microsoft.com/products/service-bus/).

Dieser Connector verbindet den Azure Service Bus. Er unterstützt mehrere
sendende und empfangende Verbindungen zu Queues und Topics.

Zusätzlich stellt der Connector eine `IProcessStartEventBean` bereit,
die beim Empfang einer Azure Service Bus Message einen Ivy process starten kann.
