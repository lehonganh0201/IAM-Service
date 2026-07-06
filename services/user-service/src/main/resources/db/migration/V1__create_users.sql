CREATE TABLE users
(
    id                  UUID PRIMARY KEY,
    username            VARCHAR(100)             NOT NULL UNIQUE,
    full_name           VARCHAR(255)             NOT NULL,
    date_of_birth       DATE,
    street              VARCHAR(255),
    ward                VARCHAR(255),
    district            VARCHAR(255),
    province            VARCHAR(255),
    years_of_experience DOUBLE PRECISION,
    avatar_file_id      UUID,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE,
    status              VARCHAR(16)              NOT NULL
);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_province ON users (province);
CREATE INDEX idx_users_status ON users (status);
