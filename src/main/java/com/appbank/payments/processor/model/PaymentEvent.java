package com.appbank.payments.processor.model;

/**
 * Payload published to payments.created.v1 / payments.retry.1 / payments.retry.2.
 */
public class PaymentEvent {

    private String correlationId;
    private String transactionId;

    public PaymentEvent() {
    }

    public PaymentEvent(String correlationId, String transactionId) {
        this.correlationId = correlationId;
        this.transactionId = transactionId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
