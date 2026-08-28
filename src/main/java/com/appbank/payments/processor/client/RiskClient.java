package com.appbank.payments.processor.client;

import com.appbank.payments.processor.model.Payment;
import com.appbank.payments.processor.model.RiskDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Calls the internal risk service, which in turn validates the payment
 * against the external risk provider.
 *
 * version: 2.4.0 (deployed 2026-08-27, 07:00Z)
 */
@Component
public class RiskClient {

    private static final Logger log = LoggerFactory.getLogger(RiskClient.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;

    private final RestTemplate restTemplate;
    private final String riskServiceUrl;

    public RiskClient(RestTemplate restTemplate,
                       @Value("${risk-service.base-url}") String riskServiceUrl) {
        this.restTemplate = restTemplate;
        this.riskServiceUrl = riskServiceUrl;
    }

    public RiskDecision validate(Payment payment) {
        int attempt = 1;

        while (true) {
            long startedAt = System.currentTimeMillis();
            try {
                log.info("event=REST_REQUEST target=risk-service attempt={}", attempt);

                RiskDecision decision = restTemplate.postForObject(
                        riskServiceUrl + "/risk/validate",
                        payment,
                        RiskDecision.class);

                log.info("event=REST_RESPONSE target=risk-service attempt={} status=200 durationMs={}",
                        attempt, System.currentTimeMillis() - startedAt);

                return decision;

            } catch (Exception e) {
                long durationMs = System.currentTimeMillis() - startedAt;

                Integer statusCode = null;
                String retryAfter = null;
                if (e instanceof HttpStatusCodeException hsce) {
                    statusCode = hsce.getStatusCode().value();
                    HttpHeaders headers = hsce.getResponseHeaders();
                    retryAfter = headers != null ? headers.getFirst("Retry-After") : null;
                }

                // Status and Retry-After are captured here for observability,
                // but nothing below branches on them: every exception is
                // treated the same way regardless of what it actually means.
                log.warn("event=REST_RESPONSE target=risk-service attempt={} status={} retryAfterSec={} durationMs={}",
                        attempt, statusCode, retryAfter, durationMs);

                if (attempt >= MAX_ATTEMPTS) {
                    log.error("event=RETRY_EXHAUSTED maxAttempts={} exception={}",
                            MAX_ATTEMPTS, e.getClass().getSimpleName());
                    log.error("event=RISK_VALIDATION_FAILED message=\"Risk validation failed after {} attempts\"",
                            MAX_ATTEMPTS);
                    throw new RetryableRiskException(
                            "Risk validation failed after " + MAX_ATTEMPTS + " attempts", e);
                }

                log.warn("event=RETRY_SCHEDULED attempt={} delayMs={} classification=RETRYABLE",
                        attempt + 1, RETRY_DELAY_MS);

                attempt++;
                sleep(RETRY_DELAY_MS);
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Thrown when risk validation could not be completed after exhausting
     * retries. Currently thrown for every failure category (see above).
     */
    public static class RetryableRiskException extends RuntimeException {
        public RetryableRiskException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
