alter table users
    add column if not exists deleted_at timestamp;

alter table users
    add column if not exists deleted_by bigint;

alter table users
    add column if not exists deletion_reason varchar(500);

alter table roles
    add column if not exists deleted_at timestamp;

alter table roles
    add column if not exists deleted_by bigint;

alter table roles
    add column if not exists deletion_reason varchar(500);

alter table permissions
    add column if not exists deleted_at timestamp;

alter table permissions
    add column if not exists deleted_by bigint;

alter table permissions
    add column if not exists deletion_reason varchar(500);

update users
set deleted = false
where deleted is null;

update roles
set deleted = false
where deleted is null;

update permissions
set deleted = false
where deleted is null;