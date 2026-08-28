-- Seed data for local runs. Loosely aligned with 03_production.log so the
-- data feels coherent if the candidate inspects the database.
INSERT INTO payments (id, transaction_id, account_id, amount, currency, status, created_at, updated_at) VALUES
(1, 'TX928731', 'ACC-55210', 1450.00, 'MXN', 'PROCESSING', TIMESTAMP '2026-08-27 10:12:00', TIMESTAMP '2026-08-27 10:12:00'),
(2, 'TX184514', 'ACC-30045', 890.00,  'MXN', 'APPROVED',   TIMESTAMP '2026-08-27 10:02:27', TIMESTAMP '2026-08-27 10:02:27'),
(3, 'TX930442', 'ACC-90911', 310.00,  'MXN', 'PROCESSING', TIMESTAMP '2026-08-27 10:15:20', TIMESTAMP '2026-08-27 10:15:20'),
(4, 'TX551200', 'ACC-55210', 220.00,  'MXN', 'COMPLETED',  TIMESTAMP '2026-08-26 16:40:00', TIMESTAMP '2026-08-26 16:40:05'),
(5, 'TX551201', 'ACC-55210', 75.50,   'MXN', 'PENDING',    TIMESTAMP '2026-08-27 08:05:00', TIMESTAMP '2026-08-27 08:05:00');
