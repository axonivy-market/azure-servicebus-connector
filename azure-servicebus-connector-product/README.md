# Azure Service Bus Connector

Connect to the [Azure Service Bus](https://azure.microsoft.com/products/service-bus/).

This connector gives you access to the Azure Service Bus. It allows to
define multiple sending and receiving connections to queues and topics.

Additionally, this connector provides an `IProcessStartEventBean` which
can be used to start Ivy processes which react on Azure Service Bus
messages.

## Demo

The demo provides a dialog with buttons for sending and receiving messages
for different configurations.

To see the effect of sending messages you have multiple options. In the GUI
use the button to receive a maximum number of messages from a configuration
within a max wait time.

### AzureServiceBusStartEventBean

The demo also contains examples of using an `AzureServiceBusStartEventBean`.
In the Demo, the bean will listen for configuration `demo2` and simply
log the message in the run-time log.

### Other tools

Use the Azure Servie Bus Explorer in the Azure Portal to see and send messages.

## Setup

Unpack the demo project to adapt all values described below.

### Azure Service Portal

In the Azure Service Portal create a namespace and queues and topics. The
demo expects queues `demo1`, `demo2` and topic `demo3` with a subscription
name `demosubscription`.

## Usage

The connector currently supports connecting with a `connectionString` or with the
`fullyQualifiedName` and a `DefaultAzureCredentialBuilder`. Both values can be
configured for every configuration. If you want to share this configuration between
multiple queues or topics, it is a good idea to create a base configuration and
`iherit` from it in other places. Please see the Azure documentation for a description
of these methods.

The connector provides simple send and receive methods and automatically creates senders
and receivers based on global variables configuration.

If any special configuration is needed, please implement an `AzureServiceBusSenderSupplier`
which creates a sender or an `AzureServiceBusReceiverSupplier` which creates a receiver.
You might want to look into the `AzureServiceBusService` to see how this is done.
Senders and receivers created in by Suppliers are identified by a configuration name and
will be cached and re-used in the same way as their simple counterparts.
To create a specific processor, you should extend the provided `AzureServiceBusStartEventBean`
and override the function `createProcessor`.

### AzureServiceBusStartEventBean

An `AzureServiceBusStartEventBean` for use in an Ivy *Program start* element is provided to listen
on configurations and start Ivy processes. Select this bean in the *Start* tab of a *Program start*
element.

Configure the configuration name to use for the processor in the *Editor* tab of the *Program start* element:

### Configuration

Configuration can be done in global variables where some simple inheritence mechanism
is provided. All Azure Service Bus configuration is stored below the `azure-servicebus-connector` global
variable.


```
@variables.yaml@
```
