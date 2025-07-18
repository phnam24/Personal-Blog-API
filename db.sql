-- Tạo Cơ sở dữ liệu mới (nếu chưa có)
CREATE DATABASE IF NOT EXISTS personal_blog_db;

-- Sử dụng CSDL vừa tạo
USE personal_blog_db;

-- =============================================
-- SECTION 1: TẠO CẤU TRÚC CÁC BẢNG
-- =============================================

-- Bảng người dùng (users)
CREATE TABLE users (
                       id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER' CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN')),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng thẻ (tags)
CREATE TABLE tags (
                      id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                      name VARCHAR(50) UNIQUE NOT NULL
);

-- Bảng bài viết (posts)
CREATE TABLE posts (
                       id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       content TEXT NOT NULL,
                       author_id BIGINT UNSIGNED NOT NULL, -- SỬA: từ INTEGER sang BIGINT UNSIGNED
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       CONSTRAINT fk_author FOREIGN KEY(author_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng trung gian cho quan hệ nhiều-nhiều giữa posts và tags
CREATE TABLE post_tags (
                           post_id BIGINT UNSIGNED NOT NULL, -- SỬA
                           tag_id BIGINT UNSIGNED NOT NULL,  -- SỬA
                           PRIMARY KEY (post_id, tag_id),
                           CONSTRAINT fk_post_tags_post FOREIGN KEY(post_id) REFERENCES posts(id) ON DELETE CASCADE,
                           CONSTRAINT fk_post_tags_tag FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Bảng bình luận (comments) với parent_id
CREATE TABLE comments (
                          id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                          content TEXT NOT NULL,
                          post_id BIGINT UNSIGNED NOT NULL, -- SỬA
                          user_id BIGINT UNSIGNED NOT NULL, -- SỬA
                          parent_id BIGINT UNSIGNED,        -- SỬA, NULL nếu là bình luận gốc
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_comment_post FOREIGN KEY(post_id) REFERENCES posts(id) ON DELETE CASCADE,
                          CONSTRAINT fk_comment_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
                          CONSTRAINT fk_parent_comment FOREIGN KEY(parent_id) REFERENCES comments(id) ON DELETE CASCADE
);


-- =============================================
-- SECTION 2: THÊM DỮ LIỆU MẪU
-- =============================================

INSERT INTO users (username, email, password, role) VALUES
                                                        ('admin', 'admin@blog.com', '$2a$10$abcdefghijklmnopqrstuv', 'ROLE_ADMIN'),
                                                        ('user_one', 'user.one@email.com', '$2a$10$abcdefghijklmnopqrstuv', 'ROLE_USER'),
                                                        ('user_two', 'user.two@email.com', '$2a$10$abcdefghijklmnopqrstuv', 'ROLE_USER');

INSERT INTO tags (name) VALUES
                            ('Java'), ('Spring Boot'), ('Database'), ('Security'), ('Tutorial');

INSERT INTO posts (title, content, author_id) VALUES
                                                  ('Hướng dẫn Spring Boot cho người mới bắt đầu', 'Nội dung chi tiết về cách tạo một dự án Spring Boot...', 1),
                                                  ('Tìm hiểu về Spring Security và JWT', 'Spring Security là một framework mạnh mẽ. JWT được dùng để xác thực...', 1),
                                                  ('Tối ưu hóa câu lệnh SQL trong MySQL', 'Để tăng hiệu năng, chúng ta cần viết các câu lệnh SQL hiệu quả...', 2),
                                                  ('5 sai lầm phổ biến khi lập trình Java', 'Dưới đây là 5 sai lầm mà các lập trình viên Java thường mắc phải...', 3);

INSERT INTO post_tags (post_id, tag_id) VALUES
                                            (1, 1), (1, 2), (1, 5),
                                            (2, 2), (2, 4),
                                            (3, 1), (3, 3),
                                            (4, 1);

INSERT INTO comments (content, post_id, user_id, parent_id) VALUES
                                                                ('Bài viết rất hay và chi tiết!', 1, 2, NULL),
                                                                ('Cảm ơn bạn nhé!', 1, 3, NULL),
                                                                ('Đúng vậy, mình đã làm theo và thành công!', 1, 3, 1),
                                                                ('Phần JWT giải thích hơi khó hiểu một chút.', 2, 3, NULL),
                                                                ('Cảm ơn góp ý của bạn, mình sẽ cập nhật lại cho rõ hơn.', 2, 1, 4),
                                                                ('Mình cũng thấy vậy, mong chờ bản cập nhật.', 2, 3, 5),
                                                                ('Tuyệt vời, đúng thứ mình đang tìm kiếm!', 3, 1, NULL);