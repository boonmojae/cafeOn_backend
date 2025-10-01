CREATE DATABASE IF NOT EXISTS cafeOn
    DEFAULT CHARACTER SET utf8
    DEFAULT COLLATE utf8_general_ci;

USE cafeOn;

-- users
CREATE TABLE IF NOT EXISTS users(
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

DESC users;

-- cafes
CREATE TABLE IF NOT EXISTS cafes (
    cafe_id BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 내부 서비스 PK
    kakao_id VARCHAR(50) UNIQUE,                    -- 카카오 API ID (없으면 NULL)
    name VARCHAR(255) NOT NULL,                     -- 카페 이름
    address VARCHAR(500) NOT NULL,                  -- 카페 주소(도로명 or 지번)
    latitude DECIMAL(20, 15) NOT NULL,              -- 위도
    longitude DECIMAL(20, 15) NOT NULL,             -- 경도
    phone VARCHAR(50),                              -- 전화번호
    open_hours TEXT,                                -- 오픈 시간
    avg_rating DECIMAL(3,2),                        -- 평균 별점(ex. 3.44)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    kakao_url VARCHAR(255),                         -- 카카오맵 URL
    source ENUM('KAKAO', 'USER') DEFAULT 'KAKAO'    -- 데이터 출처
);

DESC cafes;