CREATE DATABASE IF NOT EXISTS cafeOn
    DEFAULT CHARACTER SET utf8
    DEFAULT COLLATE utf8_general_ci;

USE cafeOn;

CREATE TABLE IF NOT EXISTS users(
    user_id CHAR(36) PRIMARY KEY,   -- UUID
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    nickname VARCHAR(50),
    profile_image JSON,
    status ENUM('ACTIVE', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    provider ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER') NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    preference_keywords JSON,
    refresh_token VARCHAR(512),
    penalty_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

DESC user;

CREATE TABLE IF NOT EXISTS cafes (
    cafe_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 내부 서비스 PK
    kakao_id VARCHAR(50) UNIQUE, -- 카카오 API ID (없으면 NULL)
    name VARCHAR(255) NOT NULL, -- 카페 이름
    address VARCHAR(500) NOT NULL, -- 카페 주소(도로명 or 지번)
    latitude DECIMAL(20, 15) NOT NULL, -- 위도
    longitude DECIMAL(20, 15) NOT NULL, -- 경도
    phone VARCHAR(50), -- 전화번호
    open_hours TEXT, -- 오픈 시간
    avg_rating DECIMAL(3,2), -- 평균 별점(ex. 3.44)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    kakao_url VARCHAR(255), -- 카카오맵 URL
    source ENUM('KAKAO', 'USER') DEFAULT 'KAKAO' -- 데이터 출처
);

CREATE TABLE IF NOT EXISTS posts (
  post_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id     CHAR(36)         NOT NULL,
  type        ENUM('GENERAL','QUESTION','INFO') NOT NULL DEFAULT 'GENERAL',
  title       VARCHAR(255)     NOT NULL,
  content     TEXT             NOT NULL,
  created_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  view_count  BIGINT           NOT NULL DEFAULT 0,

  INDEX idx_posts_user_id (user_id),
  INDEX idx_posts_created_at (created_at),

  CONSTRAINT fk_posts_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post_likes (
  post_like_id BIGINT      NOT NULL AUTO_INCREMENT,
  post_id      BIGINT      NOT NULL,
  user_id      CHAR(36)    NOT NULL,
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (post_like_id),
  CONSTRAINT uk_post_like_post_user UNIQUE (post_id, user_id),

  INDEX idx_post_likes_post_id (post_id),
  INDEX idx_post_likes_user_id (user_id),

  CONSTRAINT fk_post_likes_post
    FOREIGN KEY (post_id) REFERENCES posts(post_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_post_likes_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
  comment_id   BIGINT      NOT NULL AUTO_INCREMENT,
  post_id      BIGINT      NOT NULL,
  user_id      CHAR(36)    NOT NULL,
  parent_id    BIGINT      NULL,
  content      TEXT        NOT NULL,
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (comment_id),

  INDEX idx_comments_post_id   (post_id),
  INDEX idx_comments_user_id   (user_id),
  INDEX idx_comments_parent_id (parent_id),
  INDEX idx_comments_created_at(created_at),

  CONSTRAINT fk_comments_post
    FOREIGN KEY (post_id) REFERENCES posts(post_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_comments_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_comments_parent
    FOREIGN KEY (parent_id) REFERENCES comments(comment_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comment_likes (
  comment_like_id BIGINT     NOT NULL AUTO_INCREMENT,
  comment_id      BIGINT     NOT NULL,
  user_id         CHAR(36)   NOT NULL,
  created_at      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (comment_like_id),
  CONSTRAINT uk_comment_like_comment_user UNIQUE (comment_id, user_id),

  INDEX idx_comment_likes_comment_id (comment_id),
  INDEX idx_comment_likes_user_id    (user_id),

  CONSTRAINT fk_comment_likes_comment
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_comment_likes_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS images (
    image_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NULL,
    stored_file_name VARCHAR(255) NULL,
    PRIMARY KEY (image_id),
    FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS questions (
  question_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,       -- PK (Java Long)
  user_id     CHAR(36) NOT NULL,                             -- 작성자(`user`.user_id)
  title       VARCHAR(150) NOT NULL,                         -- 질문 제목
  content     TEXT NOT NULL,                                 -- 질문 내용
  visibility  ENUM('PUBLIC','PRIVATE') NOT NULL DEFAULT 'PUBLIC', // 기본값 공개
  status      ENUM('PENDING','ANSWERED') NOT NULL DEFAULT 'PENDING', // 기본값 답변전
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (question_id),

  CONSTRAINT fk_q_user
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE
);
