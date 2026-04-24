CREATE TABLE active_tokens (
    jti        VARCHAR(36)  NOT NULL PRIMARY KEY,
    username   VARCHAR(64)  NOT NULL,
    expires_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_active_tokens_username   ON active_tokens(username);
CREATE INDEX idx_active_tokens_expires_at ON active_tokens(expires_at);
