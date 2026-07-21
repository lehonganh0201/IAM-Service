create table user_profiles
(
    id                    bigserial primary key,
    street                varchar(255),
    ward                  varchar(100),
    district              varchar(100),
    province              varchar(100),
    years_of_experience   double precision,
    avatar_file_id        uuid,
    created_at            timestamp not null,
    updated_at            timestamp not null,
    created_by            bigint,
    last_modified_by      bigint
);

create table users
(
    id               bigserial primary key,
    keycloak_user_id varchar(100),
    username         varchar(100) not null,
    email            varchar(255),
    first_name       varchar(100),
    last_name        varchar(100),
    password_hash    varchar(255),
    phone_number     varchar(30),
    date_of_birth    date,
    avatar_url       text,
    profile_id       bigint,
    email_verified   boolean      not null default false,
    enabled          boolean      not null default true,
    locked           boolean      not null default false,
    deleted          boolean      not null default false,
    created_at       timestamp    not null,
    updated_at       timestamp    not null,
    created_by       bigint,
    last_modified_by bigint,

    constraint fk_users_profile
        foreign key (profile_id)
            references user_profiles (id)
);

create unique index uk_users_profile_id
    on users (profile_id)
    where profile_id is not null;

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
    id               bigserial primary key,
    user_id bigint not null references users (id),
    role_id bigint not null references roles (id),
    created_at       timestamp    not null,
    updated_at       timestamp    not null,
    created_by       bigint,
    last_modified_by bigint
);

create table role_permissions
(
    id               bigserial primary key,
    role_id       bigint not null references roles (id),
    permission_id bigint not null references permissions (id),
    created_at       timestamp    not null,
    updated_at       timestamp    not null,
    created_by       bigint,
    last_modified_by bigint
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