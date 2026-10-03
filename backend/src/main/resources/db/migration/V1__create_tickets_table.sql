CREATE TABLE tickets (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    title          VARCHAR(120)  NOT NULL,
    description    VARCHAR(5000) NOT NULL,
    customer_email VARCHAR(254)  NOT NULL,
    priority       VARCHAR(20)   NOT NULL,
    status         VARCHAR(20)   DEFAULT 'OPEN' NOT NULL,
    created_at     DATETIME(6)   NOT NULL,
    updated_at     DATETIME(6)   NOT NULL
);

CREATE INDEX idx_tickets_status     ON tickets (status);
CREATE INDEX idx_tickets_priority   ON tickets (priority);
CREATE INDEX idx_tickets_created_at ON tickets (created_at);
