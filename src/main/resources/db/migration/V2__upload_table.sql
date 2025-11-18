-- Vx__upload_table.sql
-- 테이블: upload (Upload 엔티티 매핑)

CREATE TABLE IF NOT EXISTS upload (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    uploader_account_id BIGINT       NOT NULL,
    public_id           VARCHAR(255) NOT NULL,
    secure_url          VARCHAR(1000),
    original_name       VARCHAR(255),
    resource_type       VARCHAR(50),
    format              VARCHAR(50),
    bytes_size          BIGINT,
    width               INT,
    height              INT,
    folder              VARCHAR(255),
    context             VARCHAR(500),
    hidden              BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          DATETIME     NOT NULL,
    deleted_at          DATETIME NULL,
    CONSTRAINT uq_upload_public_id UNIQUE (public_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
