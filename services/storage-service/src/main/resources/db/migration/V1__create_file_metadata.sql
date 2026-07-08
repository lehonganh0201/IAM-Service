CREATE TABLE file_metadata (
                               id UUID PRIMARY KEY, original_name VARCHAR(255) NOT NULL, stored_name VARCHAR(255) NOT NULL UNIQUE,
                               extension VARCHAR(32) NOT NULL, content_type VARCHAR(255) NOT NULL, file_size BIGINT NOT NULL,
                               checksum_sha256 VARCHAR(64) NOT NULL, storage_provider VARCHAR(16) NOT NULL, bucket_name VARCHAR(255),
                               object_key VARCHAR(1000) NOT NULL, visibility VARCHAR(16) NOT NULL, owner_id VARCHAR(255) NOT NULL,
                               owner_username VARCHAR(255) NOT NULL, description VARCHAR(1000), tags VARCHAR(1000), image_width INTEGER,
                               image_height INTEGER, created_at TIMESTAMP WITH TIME ZONE NOT NULL, created_by VARCHAR(255) NOT NULL,
                               updated_at TIMESTAMP WITH TIME ZONE, updated_by VARCHAR(255), deleted_at TIMESTAMP WITH TIME ZONE,
                               deleted_by VARCHAR(255), version BIGINT, status VARCHAR(16) NOT NULL
);
CREATE INDEX idx_file_owner ON file_metadata(owner_id);
CREATE INDEX idx_file_visibility ON file_metadata(visibility);
CREATE INDEX idx_file_content_type ON file_metadata(content_type);
CREATE INDEX idx_file_created_at ON file_metadata(created_at);
