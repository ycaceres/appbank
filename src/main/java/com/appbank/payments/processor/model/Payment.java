package com.appbank.payments.processor.model;

import java.math.BigDecimal;

public class Payment {

    private Long id;
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    public Payment() {
    }

    public Payment(Long id, String transactionId, String accountId,
                    BigDecimal amount, String currency, PaymentStatus status) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
