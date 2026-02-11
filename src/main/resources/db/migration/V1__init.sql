CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_identities (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    signin_type VARCHAR(255) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    platform VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    package_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_identities_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
