package com.appbank.payments.processor.model;

public class RiskDecision {

    private boolean approved;
    private String reasonCode;

    public RiskDecision() {
    }

    public RiskDecision(boolean approved, String reasonCode) {
        this.approved = approved;
        this.reasonCode = reasonCode;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }
}
