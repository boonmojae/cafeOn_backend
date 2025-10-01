CREATE DATABASE IF NOT EXISTS cafeOn
    DEFAULT CHARACTER SET utf8
    DEFAULT COLLATE utf8_general_ci;

USE cafeOn;

CREATE TABLE IF NOT EXISTS user(
    user_id CHAR(36) PRIMARY KEY,   -- UUID
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

CREATE TABLE IF NO EXISTS posts(
    post_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id CHAR(36) NULL,
    type ENUM('GENERAL', 'QUESTION', 'INFO') NOT NULL DEFAULT 'GENERAL',
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (post_id),
    FOREIGN KEY (user_id) REFERENCES user (user_id)
)

CREATE TABLE IF NOT EXISTS images (
    image_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NULL,
    stored_file_name VARCHAR(255) NULL,
    PRIMARY KEY (image_id),
    FOREIGN KEY (post_id) REFERENCES posts (post_id) ON DELETE CASCADE
);