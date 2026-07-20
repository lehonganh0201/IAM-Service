insert into permissions(code, name, description, deleted, created_at, updated_at)
values ('USER_CREATE', 'Create user', 'Create user', false, now(), now()),
       ('USER_READ', 'Read user', 'Read user', false, now(), now()),
       ('USER_UPDATE', 'Update user', 'Update user', false, now(), now()),
       ('USER_DELETE', 'Delete user', 'Delete user', false, now(), now()),
       ('USER_LOCK', 'Lock user', 'Lock user', false, now(), now()),
       ('USER_UNLOCK', 'Unlock user', 'Unlock user', false, now(), now()),
       ('USER_RESET_PASSWORD', 'Reset user password', 'Reset user password', false, now(), now()),
       ('USER_ASSIGN_ROLE', 'Assign role to user', 'Assign role to user', false, now(), now()),
       ('ROLE_CREATE', 'Create role', 'Create role', false, now(), now()),
       ('ROLE_READ', 'Read role', 'Read role', false, now(), now()),
       ('ROLE_UPDATE', 'Update role', 'Update role', false, now(), now()),
       ('ROLE_DELETE', 'Delete role', 'Delete role', false, now(), now()),
       ('ROLE_ASSIGN_PERMISSION', 'Assign permission to role', 'Assign permission to role', false, now(), now()),
       ('PERMISSION_CREATE', 'Create permission', 'Create permission', false, now(), now()),
       ('PERMISSION_READ', 'Read permission', 'Read permission', false, now(), now()),
       ('PERMISSION_UPDATE', 'Update permission', 'Update permission', false, now(), now()),
       ('PERMISSION_DELETE', 'Delete permission', 'Delete permission', false, now(), now()) on conflict do nothing;

insert into roles(code, name, description, deleted, created_at, updated_at)
values ('USER_MANAGER', 'User Manager', 'Manage users', false, now(), now()),
       ('SYSTEM_ADMIN', 'System Admin', 'Manage roles and permissions', false, now(), now()) on conflict do nothing;

insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on p.code in (
                                          'USER_CREATE',
                                          'USER_READ',
                                          'USER_UPDATE',
                                          'USER_DELETE',
                                          'USER_LOCK',
                                          'USER_UNLOCK',
                                          'USER_RESET_PASSWORD',
                                          'USER_ASSIGN_ROLE'
    )
where r.code = 'USER_MANAGER' on conflict do nothing;

insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on p.code in (
                                          'ROLE_CREATE',
                                          'ROLE_READ',
                                          'ROLE_UPDATE',
                                          'ROLE_DELETE',
                                          'ROLE_ASSIGN_PERMISSION',
                                          'PERMISSION_CREATE',
                                          'PERMISSION_READ',
                                          'PERMISSION_UPDATE',
                                          'PERMISSION_DELETE'
    )
where r.code = 'SYSTEM_ADMIN' on conflict do nothing;