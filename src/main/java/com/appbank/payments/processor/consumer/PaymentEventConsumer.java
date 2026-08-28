package com.appbank.payments.processor.consumer;

import com.appbank.payments.processor.model.PaymentEvent;
import com.appbank.payments.processor.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes payment lifecycle events from Kafka and delegates processing
 * to {@link PaymentService}.
 *
 * Listens to the primary topic as well as the retry topics used by the
 * platform's dead-letter/retry strategy:
 *
 *   payments.created.v1  -> first delivery
 *   payments.retry.1     -> first redelivery after a processing failure
 *   payments.retry.2     -> second redelivery after a processing failure
 *
 * Messages that fail again after payments.retry.2 are routed by the
 * platform's retry framework to payments.dlq and are not consumed here.
 *
 * correlationId / transactionId are placed in the MDC for the duration
 * of processing, so every log statement further down the call stack
 * (PaymentService, RiskClient, ...) carries them automatically — see
 * logback-spring.xml.
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentService paymentService;

    public PaymentEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = {
            "payments.created.v1",
            "payments.retry.1",
            "payments.retry.2"
    }, groupId = "payment-processor-group")
    public void onPaymentEvent(ConsumerRecord<String, PaymentEvent> record) {
        PaymentEvent event = record.value();

        MDC.put("correlationId", event.getCorrelationId());
        MDC.put("transactionId", event.getTransactionId());

        try {
            log.info("event=KAFKA_CONSUME topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset());

            paymentService.process(event);

        } catch (Exception e) {
            // A processing failure here causes the consumer to NACK the
            // message, which the platform's Kafka retry policy will
            // redeliver via payments.retry.1 / payments.retry.2 before
            // finally routing it to payments.dlq.
            log.error("event=PAYMENT_EVENT_PROCESSING_FAILED topic={} error={}",
                    record.topic(), e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
