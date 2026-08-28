package com.appbank.payments.processor.repository;

import com.appbank.payments.processor.model.Payment;
import com.appbank.payments.processor.model.PaymentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC access to the PAYMENTS table (Oracle).
 *
 * Table size: ~85,000,000 rows.
 */
@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Payment findByTransactionId(String transactionId) {
        String sql = "SELECT id, transaction_id, account_id, amount, currency, status, created_at " +
                     "FROM payments WHERE transaction_id = ?";
        List<Payment> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> mapRow(rs), transactionId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void updateStatus(Long paymentId, PaymentStatus status) {
        String sql = "UPDATE payments SET status = ?, updated_at = SYSTIMESTAMP WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), paymentId);
    }

    /**
     * Returns the open (non-completed) payments for a given account, most
     * recent first. Used by the customer-facing "payment history" screen
     * and by internal support tooling.
     */
    public List<Payment> findOpenPaymentsByAccount(String accountId) {
        String sql = "SELECT * " +
                     "FROM payments " +
                     "WHERE account_id = ? " +
                     "AND status <> 'COMPLETED' " +
                     "ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), accountId);
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setAccountId(rs.getString("account_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setCurrency(rs.getString("currency"));
        p.setStatus(PaymentStatus.valueOf(rs.getString("status")));
        return p;
    }
}
