# payment-processor

Payment Processor service for the payments platform. Consumes payment
events from Kafka, validates them against the internal Risk Service, and
updates the payment status.

This repository is part of a technical interview exercise. See the
production incident description you were given for context on what to
investigate.

## Requirements

- Java 17
- Maven 3.9+

## Build

```bash
mvn clean install
```

## Run tests

```bash
mvn test
```

You should see the existing test suite pass as-is.

## Run locally

The datasource is an in-memory H2 database (running in Oracle
compatibility mode) that is seeded automatically on startup from
`schema.sql` / `data.sql` — no real Oracle instance is required to run
the application.

Kafka still points to `localhost:9092`. If no broker is running locally,
the consumer will log connection retries in the background, but this
does not prevent the application from starting.

```bash
mvn spring-boot:run
```

In a real environment, the H2 datasource is replaced by the actual
Oracle connection.

## Project structure

```
src/main/java/com/appbank/payments/processor/
├── PaymentProcessorApplication.java
├── config/
│   └── RestClientConfig.java
├── consumer/
│   └── PaymentEventConsumer.java        Kafka listener (created + retry topics)
├── service/
│   └── PaymentService.java              Orchestrates load -> validate -> update -> publish
├── client/
│   └── RiskClient.java                  Calls the Risk Service
├── repository/
│   └── PaymentRepository.java           JDBC access to the payments table
└── model/
    ├── Payment.java
    ├── PaymentEvent.java
    ├── PaymentStatus.java
    └── RiskDecision.java

src/main/resources/
├── application.yml
├── logback-spring.xml                   Log format (adds class/method/correlationId/transactionId)
├── schema.sql
└── data.sql

src/test/java/com/appbank/payments/processor/
└── client/
    └── RiskClientTest.java
```
