# IAM Service

Backend IAM service bằng Spring Boot, hỗ trợ 2 chế độ xác thực:

- **Self-IDP mode**: IAM service tự xác thực bằng database nội bộ, phát hành JWT access token và refresh token.
- **Keycloak mode**: Keycloak là Identity Server; backend validate Bearer token bằng OAuth2 Resource Server, sau đó kiểm tra user/role/permission trong database IAM nội bộ.

RBAC của hệ thống **luôn lấy từ database IAM**, không phụ thuộc hoàn toàn vào role trên Keycloak.
## 1. Công nghệ sử dụng

| Thành phần |                            Version |
|---|-----------------------------------:|
| Java |                                 17 |
| Spring Boot |                             4.0.6 |
| Spring Security | 4.0.6, do Spring Boot BOM quản lý |
| Keycloak Server |                             26.0.9 |
| keycloak-admin-client |                             26.0.9 |
| springdoc-openapi |                             2.7.0 |
| PostgreSQL |                               42.7.10 |
| Flyway | 11.14.1, do Spring Boot BOM quản lý |
| Logback | 1.5.34, do Spring Boot BOM quản lý |

## 2. Kiến trúc tổng quan

```text
Client / Swagger / Postman
        |
        v
Spring Security OAuth2 Resource Server
        |
        +-- Self-IDP: validate JWT HS256 do IAM phát hành
        |
        +-- Keycloak mode: validate JWT từ Keycloak issuer
        |
        v
IAM user status check: enabled / locked / deleted
        |
        v
@PreAuthorize("hasPermission(null, 'PERMISSION_CODE')")
        |
        v
DatabasePermissionEvaluator -> permissions trong PostgreSQL
        |
        v
Controller -> Service -> Repository -> PostgreSQL
```

## 3. Cấu hình mode xác thực

Trong `application.yml`:

```yaml
app:
  identity-provider:
    type: self # hoặc keycloak
```

## 4. Chạy PostgreSQL và Keycloak bằng Docker Compose

```bash
cp .env.example .env
# chỉnh .env nếu cần
docker compose up -d
```

Dịch vụ:

- IAM PostgreSQL: `localhost:5432`
- Keycloak PostgreSQL: `localhost:5433`
- Keycloak: `http://localhost:8081`

## 5. Chạy Spring Boot local

```bash
cp .env.example .env
./mvnw clean test
./mvnw spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Health check:

```text
http://localhost:8080/actuator/health
```

## 6. Self-IDP mode

Đặt trong `.env`:

```bash
APP_IDP_TYPE=self
APP_ADMIN_PASSWORD=Admin@123456
APP_JWT_SECRET=<base64-secret-at-least-256-bit>
```

Khi app khởi động, Flyway seed permission/role mặc định. `SeedDataRunner` tạo tài khoản admin local/dev từ biến môi trường, không hard-code password trong source code.

Tài khoản demo local/dev theo `.env.example`:

```text
username: admin
password: Admin@123456
```

### API Auth Self-IDP

Register:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","email":"demo@example.com","fullName":"Demo User","password":"Password@123"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin@123456"}'
```

Refresh:

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refresh_token>"}'
```

Logout:

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"<refresh_token>"}'
```

## 7. Keycloak mode

### 7.1. Tạo realm/client tự động

Sau khi `docker compose up -d`, chạy:

```bash
source .env 2>/dev/null || true
./scripts/keycloak-setup.sh
```

Script tạo:

- Realm: `iam-realm`
- Public client cho user login: `iam-user-client`
- Confidential service account client cho Admin API: `iam-admin-client`
- Gán client roles của `realm-management`: `manage-users`, `query-users`, `view-users`

### 7.2. Bật Keycloak mode

```bash
APP_IDP_TYPE=keycloak
APP_KEYCLOAK_AUTH_SERVER_URL=http://localhost:8081
APP_KEYCLOAK_REALM=iam-realm
APP_KEYCLOAK_USER_CLIENT_ID=iam-user-client
APP_KEYCLOAK_ADMIN_CLIENT_ID=iam-admin-client
APP_KEYCLOAK_ADMIN_CLIENT_SECRET=change-me-local-dev
```

### 7.3. Login URL

```bash
curl http://localhost:8080/api/auth/login
```

Response:

```json
{
  "message": "Please login via Keycloak",
  "loginUrl": "http://localhost:8081/realms/iam-realm/protocol/openid-connect/auth?..."
}
```

Client đăng nhập ở Keycloak, lấy `access_token`, sau đó gọi API protected:

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <keycloak_access_token>"
```

Backend sẽ validate token từ Keycloak, sau đó kiểm tra user có tồn tại trong bảng `users`, không locked/deleted/disabled, rồi mới kiểm tra permission từ database IAM.

## 8. API chính

### Auth

| Method | Path | Public | Mô tả |
|---|---|---:|---|
| GET | `/api/auth/login` | Có | Trả Keycloak login URL khi Keycloak mode |
| POST | `/api/auth/register` | Có | Register user |
| POST | `/api/auth/login` | Có | Self-IDP login |
| POST | `/api/auth/refresh` | Có | Refresh token |
| POST | `/api/auth/logout` | Có | Logout/revoke refresh token |

### Users

| Method | Path | Permission |
|---|---|---|
| POST | `/api/users` | USER_CREATE |
| GET | `/api/users` | USER_READ |
| GET | `/api/users/{id}` | USER_READ |
| DELETE | `/api/users/{id}` | USER_DELETE |
| PATCH | `/api/users/{id}/lock` | USER_LOCK |
| PATCH | `/api/users/{id}/unlock` | USER_UNLOCK |
| PATCH | `/api/users/{id}/reset-password` | USER_RESET_PASSWORD |
| POST | `/api/users/{id}/roles` | USER_ASSIGN_ROLE |
| DELETE | `/api/users/{id}/roles/{roleId}` | USER_ASSIGN_ROLE |

### Roles

| Method | Path | Permission |
|---|---|---|
| POST | `/api/roles` | ROLE_CREATE |
| GET | `/api/roles` | ROLE_READ |
| GET | `/api/roles/{id}` | ROLE_READ |
| PUT | `/api/roles/{id}` | ROLE_UPDATE |
| DELETE | `/api/roles/{id}` | ROLE_DELETE |
| POST | `/api/roles/{roleId}/permissions` | ROLE_ASSIGN_PERMISSION |
| DELETE | `/api/roles/{roleId}/permissions/{permissionId}` | ROLE_ASSIGN_PERMISSION |

### Permissions

| Method | Path | Permission |
|---|---|---|
| POST | `/api/permissions` | PERMISSION_CREATE |
| GET | `/api/permissions` | PERMISSION_READ |
| GET | `/api/permissions/{id}` | PERMISSION_READ |
| PUT | `/api/permissions/{id}` | PERMISSION_UPDATE |
| DELETE | `/api/permissions/{id}` | PERMISSION_DELETE |

## 9. Pagination

List API hỗ trợ:

```text
?page=0&size=10&keyword=admin&sort=createdAt,desc
```

Response:

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "last": false
}
```

## 10. RBAC và hasPermission

Controller dùng method security:

```java
@PreAuthorize("hasPermission(null, 'USER_CREATE')")
```

`DatabasePermissionEvaluator` đọc permission từ database qua quan hệ:

```text
users -> user_roles -> roles -> role_permissions -> permissions
```

Có cache `userPermissions`, cache được evict khi gán/xóa role/permission hoặc lock/delete user.

## 11. Soft delete

- `DELETE` không xóa vật lý bảng chính.
- `users`, `roles`, `permissions` có cột `deleted`.
- List mặc định chỉ lấy `deleted=false`.
- Unique constraint dùng partial unique index cho bản ghi chưa deleted.

## 12. Auditor

Có `@EnableJpaAuditing` và `AuditorAware`.

Ghi nhận:

- `createdAt`
- `updatedAt`
- `createdBy`
- `updatedBy`

Nếu chưa login: `system`. Nếu đã login: lấy username từ `IamPrincipal`.

## 13. Logging

Logback rolling theo ngày và theo size:

- file hiện tại: `logs/IAMService.log`
- archive: `logs/archive/IAMService.yyyy-MM-dd.i.log.gz`
- lưu tối đa 30 ngày
- tổng dung lượng tối đa 2GB

Request logging có:

- method
- URI
- query đã mask sensitive field
- status
- duration
- requestId/correlationId

Không log password, access token, refresh token, Authorization header, Cookie, client secret.
