# Personal-Blog-API

![Java 17+](https://img.shields.io/badge/Java-17+-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)

API backend cho blog cá nhân xây dựng bằng **Java Spring Boot**.  
Quản lý **bài viết, thẻ (tags), người dùng, bình luận**; hỗ trợ **xác thực JWT** (login / refresh / logout / logout-all).

---

## Mục lục
- [Tính năng](#tính-năng)
- [Tech stack](#tech-stack)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cấu hình nhanh](#cấu-hình-nhanh)
- [Chạy dự án](#chạy-dự-án)
- [Tài liệu API](#tài-liệu-api)
- [Xác thực & Quy ước](#xác-thực--quy-ước)
- [Ví dụ gọi API nhanh](#ví-dụ-gọi-api-nhanh)
- [License](#license)

---

## Tính năng
- Đăng ký / đăng nhập / đổi mật khẩu / refresh token / đăng xuất (1 thiết bị hoặc tất cả).
- CRUD **Posts**, **Tags**, **Comments**; search & phân trang.
- Lấy thông tin người dùng, cập nhật, xóa, tìm kiếm.

---

## Tech stack
- **Java 17+**, **Spring Boot** (Web, Security), **JPA/Hibernate**
- **MySQL** (hoặc MariaDB)
- **JWT** (Bearer)
- **springdoc-openapi** / Swagger UI (gợi ý)

---

## Yêu cầu hệ thống
- JDK 17+
- Maven 3.9+ (hoặc Gradle tương đương)
- MySQL 8.x (hoặc dùng Docker cho DB)
- (Tuỳ chọn) Docker & Docker Compose

---

## Cấu hình nhanh

Tạo DB và user (ví dụ MySQL):
```sql
CREATE DATABASE personal_blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'blog_user'@'%' IDENTIFIED BY 'blog_pass';
GRANT ALL PRIVILEGES ON personal_blog_db.* TO 'blog_user'@'%';
FLUSH PRIVILEGES;
```

`application.properties` (ví dụ tối thiểu):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/personal_blog_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=blog_user
spring.datasource.password=blog_pass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Base context (khớp servers trong OpenAPI)
server.servlet.context-path=/personal_blog_api
# Swagger (nếu dùng springdoc)
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# JWT
app.jwt.secret=change_me_to_a_long_random_secret
app.jwt.access-token-exp=900          # 15 phút
app.jwt.refresh-token-exp=1209600     # 14 ngày
```

---

## Chạy dự án

```bash
mvn clean spring-boot:run
# hoặc
mvn clean package && java -jar target/personal-blog-api-*.jar
```
Mặc định phục vụ tại:  
`http://localhost:8080/personal_blog_api`

---

## API Document

OpenAPI 3.1 (rút gọn từ spec bạn cung cấp):

- **Base URL (Local)**: `http://localhost:8080/personal_blog_api/api`
- **Swagger UI** (nếu bật springdoc): `http://localhost:8080/personal_blog_api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/personal_blog_api/v3/api-docs`

**Nhóm chính & endpoints tiêu biểu:**
- `auth-controller`: `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/logout-all`, `/auth/change-password/{userId}`, `/auth/pw-admin/{id}`
- `user-controller`: `/users`, `/users/{userId}`, `/users/search`, `/users/myInfo`
- `post-controller`: `/posts`, `/posts/{id}`, `/posts/search`, `/posts/{id}/tags/{tagId}`
- `tag-controller`: `/tags`, `/tags/{id}`, `/tags/search`
- `comment-controller`: `/posts/{postId}/comments`, `/posts/{postId}/comments/tree`, `/comments/{id}`, `/comments/{commentId}`

**Phân trang chuẩn:**
- `page` (mặc định `0`), `size` (mặc định `10`), `sort` (ví dụ `created_at,desc`)

---

## Xác thực & Quy ước

- **Security**: `Bearer <JWT>` ở **Authorization header**.
- Luồng cơ bản:
    1. `POST /auth/register`
    2. `POST /auth/login` → nhận `accessToken` (ngắn hạn) + `refreshToken` (dài hạn)
    3. Dùng `accessToken` cho các endpoint yêu cầu auth.
    4. Khi `accessToken` hết hạn: `POST /auth/refresh` (gửi `refreshToken`) để lấy cặp token mới.
    5. `POST /auth/logout` (đăng xuất một phiên) hoặc `POST /auth/logout-all?userId=...` (tất cả phiên).
- **Response chuẩn** bọc trong `ApiResponse*` gồm `data`, `message`, `meta` (phân trang).

---

## Ví dụ gọi API nhanh

> Base URL dùng **/api**: `http://localhost:8080/personal_blog_api/api`

### 1) Đăng ký & đăng nhập
```bash
# Đăng ký
curl -X POST http://localhost:8080/personal_blog_api/api/auth/register   -H "Content-Type: application/json"   -d '{
        "username":"alice",
        "password":"secret123",
        "email":"alice@example.com",
        "role":"ROLE_USER"
      }'

# Đăng nhập (nhận tokens)
curl -X POST http://localhost:8080/personal_blog_api/api/auth/login   -H "Content-Type: application/json"   -d '{
        "username":"alice",
        "password":"secret123"
      }'
```

Giả sử nhận về:
```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
  },
  "message": "OK",
  "meta": null
}
```

### 2) Refresh token
```bash
curl -X POST http://localhost:8080/personal_blog_api/api/auth/refresh   -H "Content-Type: application/json"   -d '{"refreshToken":"<REFRESH_TOKEN>"}'
```

### 3) Tạo Tag (yêu cầu Bearer)
```bash
curl -X POST http://localhost:8080/personal_blog_api/api/tags   -H "Authorization: Bearer <ACCESS_TOKEN>"   -H "Content-Type: application/json"   -d '{"tagName":"spring"}'
```

### 4) Tạo Post
```bash
curl -X POST http://localhost:8080/personal_blog_api/api/posts   -H "Authorization: Bearer <ACCESS_TOKEN>"   -H "Content-Type: application/json"   -d '{
        "title":"Hello Spring",
        "content":"Nội dung bài viết",
        "tagIds":[1,2]
      }'
```

### 5) Bình luận vào Post
```bash
curl -X POST http://localhost:8080/personal_blog_api/api/posts/1/comments   -H "Authorization: Bearer <ACCESS_TOKEN>"   -H "Content-Type: application/json"   -d '{
        "content":"Bài viết hay!",
        "parentId": null
      }'
```

### 6) Tìm kiếm bài viết
```bash
curl -X GET "http://localhost:8080/personal_blog_api/api/posts/search?page=0&size=10&sort=created_at,desc&q=spring&author_id=1&tag_id=2&created_from=2025-01-01&created_to=2025-12-31"
```

---
## 📬 Liên hệ

- Tác giả: @phnam24
- Issues / góp ý: Tạo tại tab Issues trên GitHub repo.
