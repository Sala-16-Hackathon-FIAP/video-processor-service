CREATE TABLE IF NOT EXISTS processing_jobs (
    id                UUID PRIMARY KEY,
    upload_id         UUID NOT NULL UNIQUE,
    user_id           UUID NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    source_s3_key     VARCHAR(1000) NOT NULL,
    result_s3_key     VARCHAR(1000),
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message     TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_processing_jobs_user_id   ON processing_jobs(user_id);
CREATE INDEX idx_processing_jobs_upload_id ON processing_jobs(upload_id);
CREATE INDEX idx_processing_jobs_status    ON processing_jobs(status);
