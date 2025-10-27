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
    profile_image VARCHAR(500),
    status ENUM('ACTIVE', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    provider ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER') NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    preference_keywords VARCHAR(500),
    refresh_token VARCHAR(512),
    penalty_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS cafes (
    cafe_id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 내부 서비스 PK
    kakao_id VARCHAR(50) UNIQUE, -- 카카오 API ID (없으면 NULL)
    name VARCHAR(255) NOT NULL, -- 카페 이름
    address VARCHAR(500) NOT NULL, -- 카페 주소(도로명 or 지번)
    latitude DECIMAL(20, 15) NOT NULL, -- 위도
    longitude DECIMAL(20, 15) NOT NULL, -- 경도
    phone VARCHAR(50), -- 전화번호
    open_hours TEXT, -- 오픈 시간
    kakao_rating DECIMAL(3,2), -- 카카오맵 내 후기별점
    avg_rating DECIMAL(3,2), -- 카카오맵과 네이버지도의 별점을 평균낸 최종별점(ex. 3.44)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    kakao_url VARCHAR(255), -- 카카오맵 URL
    reviews_summary TEXT,    -- 네이버블로그에 카페이름 검색해 나온 내용들을 ai로 한줄요약함
    source ENUM('KAKAO', 'USER') DEFAULT 'KAKAO' -- 데이터 출처
);

CREATE TABLE IF NOT EXISTS tags (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE cafe_tags (
    cafe_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (cafe_id, tag_id),  -- 복합PK (중복 태그 등록 방지)
    FOREIGN KEY (cafe_id) REFERENCES cafes(cafe_id) ON DELETE CASCADE,  -- 카페 삭제 시 관련 태그 자동 삭제
    FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
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
    FOREIGN KEY (review_id) REFERENCES reviews (review_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chat_rooms (
  chatroom_id   BIGINT NOT NULL AUTO_INCREMENT,
  type          VARCHAR(10) NOT NULL,
  user_small    CHAR(36) NULL,
  user_big      CHAR(36) NULL,
  cafe_id       BIGINT NULL,
  room_name     VARCHAR(100) NULL,
  max_capacity  INT NOT NULL DEFAULT 30,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (chatroom_id),
  UNIQUE KEY uk_dm_unique (type, user_small, user_big),
  UNIQUE KEY uk_cafe_one_group (type, cafe_id),
  KEY idx_chat_rooms_type (type),
  KEY idx_chat_rooms_cafe (cafe_id)
);

CREATE TABLE IF NOT EXISTS chat_room_members (
  chatroom_member_id BIGINT NOT NULL AUTO_INCREMENT,
  chatroom_id        BIGINT NOT NULL,
  user_id            CHAR(36) NOT NULL,
  joined_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_muted           TINYINT(1) NOT NULL DEFAULT 0,
  last_read_chat_id  BIGINT NULL,
  PRIMARY KEY (chatroom_member_id),
  UNIQUE KEY uk_crm_room_user (chatroom_id, user_id),
  KEY idx_crm_user (user_id),
  KEY idx_crm_room_lastread (chatroom_id, last_read_chat_id),
  CONSTRAINT fk_crm_room
    FOREIGN KEY (chatroom_id) REFERENCES chat_rooms(chatroom_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT fk_crm_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS chats (
  chat_id      BIGINT NOT NULL AUTO_INCREMENT,
  chatroom_id  BIGINT NOT NULL,
  sender_id    CHAR(36) NULL,
  message      VARCHAR(1000) NULL,
  image_url    VARCHAR(500)  NULL,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (chat_id),
  CONSTRAINT fk_chat_room
    FOREIGN KEY (chatroom_id) REFERENCES chat_rooms(chatroom_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT fk_chat_sender
    FOREIGN KEY (sender_id) REFERENCES users(user_id)
    ON DELETE SET NULL
    ON UPDATE RESTRICT,
  KEY idx_chats_room_chatid (chatroom_id, chat_id)
);

CREATE TABLE IF NOT EXISTS notifications (
  notification_id BIGINT NOT NULL AUTO_INCREMENT,
  receiver_id     CHAR(36) NOT NULL,
  chatroom_id     BIGINT NULL,
  chat_id         BIGINT NULL,
  content         VARCHAR(500) NOT NULL,
  is_read         TINYINT(1) NOT NULL DEFAULT 0,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notification_id),
  KEY idx_notis_receiver_unread (receiver_id, is_read, created_at),
  CONSTRAINT fk_noti_receiver
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT fk_noti_room
    FOREIGN KEY (chatroom_id) REFERENCES chat_rooms(chatroom_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT,
  CONSTRAINT fk_noti_chat
    FOREIGN KEY (chat_id) REFERENCES chats(chat_id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
);


CREATE TABLE IF NOT EXISTS reports (
  report_id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reporter_id      CHAR(36) NOT NULL,
  reported_user_id CHAR(36) NULL,

  target_type      ENUM('POST','COMMENT','CHAT_ROOM','REVIEW') NOT NULL,
  target_id        BIGINT UNSIGNED NOT NULL,

  content          TEXT NOT NULL,
  status           ENUM('PENDING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (report_id),

  CONSTRAINT fk_reports_reporter
    FOREIGN KEY (reporter_id)      REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_reports_reported_user
    FOREIGN KEY (reported_user_id) REFERENCES users(user_id) ON DELETE SET NULL,

  UNIQUE KEY uk_reports_once (reporter_id, target_type, target_id)
)


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

CREATE TABLE IF NOT EXISTS answers (
  answer_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  question_id BIGINT UNSIGNED NOT NULL,
  admin_id    CHAR(36)        NOT NULL,
  content     TEXT            NOT NULL,
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (answer_id),
  KEY idx_answers_question (question_id),  -- 문의 상세에서 답변 조회용

  CONSTRAINT fk_ans_question
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE,
  CONSTRAINT fk_ans_admin
    FOREIGN KEY (admin_id)   REFERENCES user(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wishlists (
  wishlist_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     CHAR(36)        NOT NULL,
  cafe_id     BIGINT UNSIGNED NOT NULL,
  category    ENUM('HIDEOUT','WORK','ATMOSPHERE','TASTE','PLANNED') NULL,
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (wishlist_id),

  -- 한 유저가 같은 카페를 중복 위시 못 하도록
  CONSTRAINT uk_wishlists_user_cafe UNIQUE (user_id, cafe_id),

  -- 조회 성능용 인덱스
  INDEX idx_wishlists_user_id (user_id),
  INDEX idx_wishlists_cafe_id (cafe_id),

  -- FK 제약
  CONSTRAINT fk_wishlists_user
    FOREIGN KEY (user_id) REFERENCES users(user_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_wishlists_cafe
    FOREIGN KEY (cafe_id) REFERENCES cafes(cafe_id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


