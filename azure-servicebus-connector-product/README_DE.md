# Azure Service Bus Connector

Verbinden Sie sich mit dem [Azure Service
Bus](https://azure.microsoft.com/products/service-bus/).

Dieser Connector ermöglicht Ihnen den Zugriff auf den Azure Service Bus. Damit
können Sie mehrere Sende- und Empfangsverbindungen zu Warteschlangen und Themen
definieren.

Zusätzlich bietet dieser Konnektor eine IProcessStartEventBean-Schnittstelle (
`)`, die zum Starten von Ivy-Prozessen verwendet werden kann, die auf Azure
Service Bus-Nachrichten reagieren.

## Demo

Die Demo bietet einen Dialog mit Schaltflächen zum Senden und Empfangen von
Nachrichten für verschiedene Konfigurationen.

Um die Wirkung des Versendens von Nachrichten zu sehen, haben Sie mehrere
Möglichkeiten. Verwenden Sie in der GUI die Schaltfläche, um innerhalb einer
maximalen Wartezeit eine maximale Anzahl von Nachrichten aus einer Konfiguration
zu empfangen.

### AzureServiceBusStartEventBean

Die Demo enthält auch Beispiele für die Verwendung von „ `“,
„AzureServiceBusStartEventBean“ und „` “. In der Demo wartet die Bean auf die
Konfiguration „ `“, „queue2“ und „` “ und protokolliert die Meldung einfach im
Laufzeitprotokoll.

### Andere Tools

Verwenden Sie den Azure Service Bus Explorer im Azure Portal, um Nachrichten
anzuzeigen und zu senden.

## Einrichtung

Entpacken Sie das Demo-Projekt, um alle unten beschriebenen Werte anzupassen.

Sie können den Connector mit dem offiziellen [Azure Service Bus
Emulator](https://github.com/Azure/azure-service-bus-emulator-installer) oder
direkt im Azure Service Portal testen.

### Azure Service Bus Emulator

- Beginnen Sie damit, das Repository zu klonen:
  https://github.com/Azure/azure-service-bus-emulator-installer
- Kopieren Sie in der Datei „ `” „ServiceBus-Emulator\Config\Config.json” „` ”
  den Block „ `” „queue.1” „` ” und erstellen Sie eine weitere Warteschlange mit
  dem Namen „ `” „queue.2” „` ” (dient zur Demonstration des automatischen
  Empfangs von Nachrichten) in der Liste der Warteschlangen.
- Wechseln Sie in das Verzeichnis „ `“ Docker-Compose-Vorlage`
- Erstellen Sie eine Datei mit dem Namen „ `.env“` mit folgendem Inhalt:

```
SQL_PASSWORD=AxonIvy-2025
ACCEPT_EULA=y
SQL_WAIT_INTERVAL=15
CONFIG_PATH=..\ServiceBus-Emulator\Config\Config.json
```

Starten Sie Docker Compose aus demselben Verzeichnis.

`docker compose -f .\docker-compose-default.yml up -d`

Konfigurieren Sie die Verbindung in Ihren globalen Variablen `demo` Abschnitt
mit `connectionString:
'Endpoint=sb://localhost;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;'`

### Azure-Dienstportal

Die Demo ist für die Verwendung mit zwei Warteschlangen (`queue.1`, `queue.2`)
und einem Thema (`topic.1`) mit einem Abonnementnamen `subscription.3`
(entsprechend der Beispielkonfiguration im Service Bus Emulator) konfiguriert.
Um die Demo mit dem echten Azure Service Bus auszuführen, können Sie entweder
die globalen Variablen an Ihre Warteschlangen und Themen anpassen oder schnell
eine eigene Beispielkonfiguration erstellen, indem Sie die folgenden Schritte
ausführen:

Starten Sie das [Azure-Portal](https://portal.azure.com) und öffnen Sie den
Abschnitt „Service Bus-Verwaltung”. ![Azure-Portal](images/portal.png) Erstellen
Sie einen Namespace. ![Namespace erstellen](images/create-namespace.png) Fügen
Sie Ihrem Namespace eine SAS-Richtlinie mit Send- und Listen-Berechtigungen
hinzu. ![SAS-Richtlinie hinzufügen](images/add-sas-policy.png) Notieren Sie sich
die Verbindungszeichenfolge.
![Verbindungszeichenfolge](images/connection-string.png) Erstellen Sie die
erforderlichen Warteschlangen und Themen. ![Warteschlangen und Themen
erstellen](images/create-queues-and-topics.png) Erstellen Sie Abonnements für
Ihre Themen. ![Abonnements erstellen](images/create-subscriptions.png)

Konfigurieren Sie die Verbindung „ `“ connectionString` (oder
`fullyQualifiedName`) im Abschnitt „ `“ demo` der globalen Variablen.

Weitere Informationen zum Azure Service Bus finden Sie in der offiziellen
Dokumentation unter
[https://learn.microsoft.com/de-at/azure/service-bus-messaging/](https://learn.microsoft.com/de-at/azure/service-bus-messaging/).

## Verwendung

Der Connector unterstützt derzeit die Verbindung mit einem `connectionString`
oder mit dem `fullyQualifiedName` und einem `DefaultAzureCredentialBuilder`.
Beide Werte können für jede Konfiguration konfiguriert werden. Wenn Sie diese
Konfiguration für mehrere Warteschlangen oder Themen freigeben möchten,
empfiehlt es sich, eine Basiskonfiguration zu erstellen und `` an anderen
Stellen davon zu erben. Eine Beschreibung dieser Methoden finden Sie in der
Azure-Dokumentation.

Der Konnektor bietet einfache Sende- und Empfangsfunktionen und erstellt
automatisch Sender und Empfänger basierend auf der Konfiguration globaler
Variablen.

Wenn eine spezielle Konfiguration erforderlich ist, implementieren Sie bitte
einen `AzureServiceBusSenderSupplier`, der einen Sender erstellt, oder einen
`AzureServiceBusReceiverSupplier`, der einen Empfänger erstellt. Sie können sich
unter `AzureServiceBusService` ansehen, wie dies funktioniert. Von Suppliers
erstellte Sender und Empfänger werden anhand eines Konfigurationsnamens
identifiziert und werden wie ihre einfachen Entsprechungen zwischengespeichert
und wiederverwendet. Um einen bestimmten Prozessor zu erstellen, sollten Sie die
bereitgestellten `AzureServiceBusStartEventBean` erweitern und die Funktion
`createProcessor` überschreiben.

### AzureServiceBusStartEventBean

Eine „ `“ „AzureServiceBusStartEventBean“ „` “ zur Verwendung in einem
Ivy-Programm „ *“ „Program start“ „* “-Element wird bereitgestellt, um
Konfigurationen abzuhören und Ivy-Prozesse zu starten. Wählen Sie diese Bean auf
der Registerkarte „ *“ „Start“ „* “ eines Ivy-Programms „ *“ „Program start“ „*
“-Elements aus.

Konfigurieren Sie den Konfigurationsnamen, der für den Prozessor im Editor „ *“
(Konfigurationsname) auf der Registerkarte „* “ (Konfigurationsname) des
Elements „ *Program start“ (Programmstart) „* “ (Konfigurationsname) verwendet
werden soll:

### Konfiguration

Die Konfiguration kann in globalen Variablen vorgenommen werden, für die ein
einfacher Vererbungsmechanismus bereitgestellt wird. Die gesamte Azure Service
Bus-Konfiguration wird unterhalb der globalen Variablen „
`azureServicebusConnector` ” gespeichert. Informationen zur Verwendung finden
Sie in den Variablenbeschreibungen.


```
@variables.yaml@
```
