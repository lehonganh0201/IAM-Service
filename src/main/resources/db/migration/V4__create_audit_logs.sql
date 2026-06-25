create table if not exists audit_logs (
                                          id bigserial primary key,

                                          actor_user_id bigint,
                                          actor_username varchar(150),
    actor_email varchar(150),
    identity_provider varchar(50),

    action varchar(100) not null,
    resource_type varchar(100),
    resource_id varchar(100),

    result varchar(50) not null,
    message varchar(500),
    error_message varchar(1000),

    http_method varchar(20),
    request_path varchar(500),
    ip_address varchar(100),
    user_agent varchar(500),
    request_id varchar(100),

    created_at timestamp not null default now()
    );

create index if not exists idx_audit_logs_actor_user_id
    on audit_logs(actor_user_id);

create index if not exists idx_audit_logs_action
    on audit_logs(action);

create index if not exists idx_audit_logs_resource
    on audit_logs(resource_type, resource_id);

create index if not exists idx_audit_logs_result
    on audit_logs(result);

create index if not exists idx_audit_logs_request_id
    on audit_logs(request_id);

create index if not exists idx_audit_logs_created_at
    on audit_logs(created_at);