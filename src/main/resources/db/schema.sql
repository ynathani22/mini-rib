CREATE TABLE IF NOT EXISTS app_user (
    id                    BIGINT        NOT NULL AUTO_INCREMENT,
    version               BIGINT        NOT NULL DEFAULT 0,
    cif                   VARCHAR(20)   NOT NULL,
    login_id              VARCHAR(50)   NOT NULL,
    password_hash         VARCHAR(100)  NOT NULL,
    full_name             VARCHAR(100)  NOT NULL,
    email                 VARCHAR(100)  NULL,
    mobile_no             VARCHAR(15)   NULL,
    status                VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
    failed_login_count    INT           NOT NULL DEFAULT 0,
    last_login_success_at DATETIME      NULL,
    last_login_failure_at DATETIME      NULL,
    created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_cif UNIQUE (cif),
    CONSTRAINT uk_app_user_login_id UNIQUE (login_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS fnd_corp_cif_account (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    version         BIGINT        NOT NULL DEFAULT 0,
    cif             VARCHAR(20)   NOT NULL,
    acc_number      VARCHAR(20)   NOT NULL,
    acc_holder_name VARCHAR(100)  NOT NULL,
    curr_code       VARCHAR(3)    NOT NULL,
    dtype           VARCHAR(10)   NOT NULL,
    status          VARCHAR(10)   NOT NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_fnd_corp_cif_account_acc_number UNIQUE (acc_number),
    INDEX idx_fnd_corp_cif_account_cif (cif)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
