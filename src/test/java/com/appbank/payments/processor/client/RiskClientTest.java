package com.appbank.payments.processor.client;

import com.appbank.payments.processor.model.Payment;
import com.appbank.payments.processor.model.PaymentStatus;
import com.appbank.payments.processor.model.RiskDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RiskClient}.
 */
class RiskClientTest {

    private static final String RISK_SERVICE_URL = "http://risk-service:8080";

    private RestTemplate restTemplate;
    private RiskClient riskClient;
    private Payment payment;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        riskClient = new RiskClient(restTemplate, RISK_SERVICE_URL);
        payment = new Payment(1L, "TX000001", "ACC-1", new BigDecimal("100.00"), "MXN", PaymentStatus.PENDING);
    }

    @Test
    void validate_returnsDecision_onFirstSuccessfulAttempt() {
        RiskDecision expected = new RiskDecision(true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(RiskDecision.class)))
                .thenReturn(expected);

        RiskDecision result = riskClient.validate(payment);

        assertEquals(expected, result);
        verify(restTemplate, times(1))
                .postForObject(anyString(), any(), eq(RiskDecision.class));
    }

    @Test
    void validate_retriesAndSucceeds_afterTransientServerError() {
        RiskDecision expected = new RiskDecision(true, "OK");
        when(restTemplate.postForObject(anyString(), any(), eq(RiskDecision.class)))
                .thenThrow(HttpServerErrorException.create(
                        HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", null, null, null))
                .thenReturn(expected);

        RiskDecision result = riskClient.validate(payment);

        assertEquals(expected, result);
        verify(restTemplate, times(2))
                .postForObject(anyString(), any(), eq(RiskDecision.class));
    }

    @Test
    void validate_throwsRetryableRiskException_afterExhaustingAttemptsOn429() {
        when(restTemplate.postForObject(anyString(), any(), eq(RiskDecision.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, null, null));

        assertThrows(RiskClient.RetryableRiskException.class,
                () -> riskClient.validate(payment));

        verify(restTemplate, times(3))
                .postForObject(anyString(), any(), eq(RiskDecision.class));
    }

    @Test
    void validate_retriesOnBadRequestResponse() {
        when(restTemplate.postForObject(anyString(), any(), eq(RiskDecision.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.BAD_REQUEST, "Bad Request", null, null, null));

        assertThrows(RiskClient.RetryableRiskException.class,
                () -> riskClient.validate(payment));

        verify(restTemplate, times(3))
                .postForObject(anyString(), any(), eq(RiskDecision.class));
    }
}
