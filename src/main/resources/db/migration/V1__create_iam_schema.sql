create table users
(
    id               bigserial primary key,
    keycloak_user_id varchar(100),
    username         varchar(100) not null,
    email            varchar(255) not null,
    first_name       varchar(100),
    last_name        varchar(100),
    password_hash    varchar(255),
    phone_number     varchar(30),
    date_of_birth    date,
    avatar_url       text,
    email_verified   boolean      not null default false,
    enabled          boolean      not null default true,
    locked           boolean      not null default false,
    deleted          boolean      not null default false,
    created_at       timestamp    not null,
    updated_at       timestamp    not null,
    created_by       bigint,
    last_modified_by bigint
);

create unique index uk_users_username_active
    on users (username) where deleted = false;

create unique index uk_users_email_active
    on users (email) where deleted = false;

create unique index uk_users_keycloak_user_id
    on users (keycloak_user_id) where keycloak_user_id is not null;

create table roles
(
    id               bigserial primary key,
    code             varchar(100) not null,
    name             varchar(150) not null,
    description      text,
    deleted          boolean      not null default false,
    created_at       timestamp    not null,
    updated_at       timestamp    not null,
    created_by       bigint,
    last_modified_by bigint
);

create unique index uk_roles_code_active
    on roles (code) where deleted = false;

create table permissions
(
    id               bigserial primary key,
    code             varchar(100) not null,
    name             varchar(150) not null,
    description      text,
    deleted          boolean      not null default false,
    created_at       timestamp    not null,
    updated_at       timestamp    not null,
    created_by       bigint,
    last_modified_by bigint
);

create unique index uk_permissions_code_active
    on permissions (code) where deleted = false;

create table user_roles
(
    user_id bigint not null references users (id),
    role_id bigint not null references roles (id),
    primary key (user_id, role_id)
);

create table role_permissions
(
    role_id       bigint not null references roles (id),
    permission_id bigint not null references permissions (id),
    primary key (role_id, permission_id)
);

create table refresh_tokens
(
    id         bigserial primary key,
    user_id    bigint       not null references users (id),
    token_hash varchar(128) not null unique,
    expires_at timestamp    not null,
    revoked    boolean      not null default false,
    created_at timestamp    not null,
    updated_at timestamp    not null
);