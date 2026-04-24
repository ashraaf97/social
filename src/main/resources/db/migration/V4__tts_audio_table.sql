CREATE TABLE tts_audio (
    donation_id BIGINT  NOT NULL PRIMARY KEY REFERENCES donations(id) ON DELETE CASCADE,
    audio_data  BYTEA   NOT NULL,
    created_at  TIMESTAMP NOT NULL
);
