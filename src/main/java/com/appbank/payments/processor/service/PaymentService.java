package com.appbank.payments.processor.service;

import com.appbank.payments.processor.client.RiskClient;
import com.appbank.payments.processor.model.Payment;
import com.appbank.payments.processor.model.PaymentEvent;
import com.appbank.payments.processor.model.PaymentStatus;
import com.appbank.payments.processor.model.RiskDecision;
import com.appbank.payments.processor.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Orchestrates payment processing:
 *   1. Load the payment
 *   2. Validate it against the risk service
 *   3. Update its status
 *   4. Publish a status-change event
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final RiskClient riskClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository,
                           RiskClient riskClient,
                           KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.riskClient = riskClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void process(PaymentEvent event) {
        Payment payment = paymentRepository.findByTransactionId(event.getTransactionId());

        if (payment == null) {
            log.warn("event=PAYMENT_NOT_FOUND");
            return;
        }

        log.info("event=PAYMENT_LOADED accountId={} amount={} currency={}",
                payment.getAccountId(), payment.getAmount(), payment.getCurrency());

        log.info("event=RISK_VALIDATION_STARTED message=\"Starting risk validation\"");

        // Any exception thrown here (including RetryableRiskException)
        // propagates up to the Kafka consumer, which will NACK the
        // message and let the platform's retry policy redeliver it.
        RiskDecision decision = riskClient.validate(payment);

        PaymentStatus newStatus = decision.isApproved()
                ? PaymentStatus.APPROVED
                : PaymentStatus.REJECTED;

        payment.setStatus(newStatus);
        paymentRepository.updateStatus(payment.getId(), newStatus);

        log.info("event=PAYMENT_STATUS_UPDATED status={}", newStatus);

        kafkaTemplate.send("payments.status.v1", payment.getTransactionId(), payment);

        log.info("event=KAFKA_PUBLISH topic=payments.status.v1 status={}", newStatus);
    }
}
