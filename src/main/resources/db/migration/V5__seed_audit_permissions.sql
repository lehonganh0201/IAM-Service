insert into permissions(code, name, description, deleted, created_at, updated_at)
values
    ('AUDIT_READ', 'Read audit logs', 'View system audit logs', false, now(), now())
    on conflict do nothing;

insert into role_permissions(role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on p.code = 'AUDIT_READ'
where r.code = 'SYSTEM_ADMIN'
    on conflict do nothing;