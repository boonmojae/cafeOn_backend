# ☕ CafeOn Backend (Spring Boot)
카페 탐색·리뷰·북마크·채팅·추천을 제공하는 백엔드 서비스입니다.
Organization에는 `frontend`와 `backend`가 분리되어 있으며, 본 레포는 **백엔드(API & WebSocket & Admin)** 입니다.
## 1) 핵심 기능 개요
- 회원/계정
  - 이메일 회원가입·로그인(Spring Security)
  - JWT 세션/토큰
  - 소셜 로그인(OAuth2: 카카오, 구글)
  - 비밀번호 찾기(이메일 인증/임시 비번)
  - soft delete
- 마이페이지
  - 프로필 수정(닉네임·선호 키워드)
  - 비밀번호 재설정(로그인/비로그인)
  - 내 리뷰·북마크·참여 채팅방
- 카페
  - 검색(이름·지역·태그)
  - 지도 좌표 기반 조회
  - 인기도(조회수·찜수)·신규 정렬
- 카페 상세
  - 기본 정보
  - 리뷰 CRUD(+ 사진 S3)
  - 북마크
  - 소셜 공유
  - 네이버 리뷰 요약(AI 연동)
  - 유사 태그 추천·인기 카페
- 채팅
  - 카페별 그룹 채팅(WebSocket)
  - 입장 제한·알림
  - 실시간 Q&A
- 추천
  - 선호 키워드·히스토리 기반(룰 기반 -> 협업필터링 확장 가능)
- 커뮤니티
  - 게시글·댓글·대댓글·좋아요
- 관리자
  - 카페 CRUD
  - 신고 리뷰 점검·삭제
  - 페널티 누적에 따른 정지 처리
## 2) 기술 스택
- Runtime/Framework
  - Java 17
  - Spring Boot 3.x
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - (Spring Validation)
  - (Spring OAuth2 Client)
  - Spring WebSocket/STOMP
- DB/Infra
  - MySQL
  - Redis(세션/캐시/레이트리밋)
  - Amazon S3(이미지)
  - (Docker)
  - (Docker Compose)
- Auth
  - JWT(Access/Refresh)
  - OAuth2(kakao, google)
- Build/Deploy
  - Gradle
  - GitHub Actions(CI)
  - (AWS EC2/RDS/S3/ALB)
- Test
  - Spring REST Docs(or OpenAPI/Swagger)
## 3) 빠른 시작
### (1) 필수 환경 변수
`.env`(로컬) 또는 배포 환경 변수로 세팅합니다.
```bash
# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=local

# DB
DB_URL=jdbc:mysql://localhost:8080/cafeOn
DB_USERNAME=app
DB_PASSWORD=secret

# JPA
JPA_HBM2DDL=update
JPA_SHOW_SQL=false

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_ISSUER=cafeOn
JWT_SECRET=please_change_this_in_prod
JWT_ACCESS_TTL_MIN=30
JWT_REFRESH_TTL_DAY=14

# OAuth2
OAUTH2_GOOGLE_CLIENT_ID=xxxx
OAUTH2_GOOGLE_CLIENT_SECRET=xxxx
OAUTH2_GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

OAUTH2_KAKAO_CLIENT_ID=xxxx
OAUTH_KAKAO_CLIENT_SECRET=xxxx
OAUTH2_KAKAO_REDIRECT_URI=http://localhost:8080/login/oauth2/code/kakao

# Mail (비번 재설정/임시 비번)
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=no-reply@cafeon.com
MAIL_PASSWORD=xxxx

# S3 (이미지 업로드)
AWS_REGION=ap-northeast-2
S3_BUCKET=cafeon-images
AWS_ACCESS_KEY_ID=xxxx
AWS_SECRET_ACCESS_KEY=xxxx
```
### (2) 로컬 실행
```bash
# 1) DB/Redis/S3 Local Mock 띄우기 (선택)
docker compose up -d

# 2) 애플리케이션 실행
./gradlew bootRun
```

### (3) 헬스 체크
```bash
curl -i http://localhost:8080/actuator/health
```

## 4) 프로젝트 구조
```bash
backend/
├─ src/main/java/com/org/cafeOn/
│   ├─ common/      # 공통(Exception, Response, Utils, Security)
│   ├─ config/      # Security, OAuth2, WebSocket, Swagger(OpenAPI)
│   ├─ domain/
│   │   ├─ user/    # User, Auth, Profile, Penalty
│   │   ├─ cafe/    # Cafe, Tag, CafeTag, Wishlist, View
│   │   ├─ review/  # Review, ReviewImage
│   │   ├─ chat/    # Chatroom, ChatMessage, Membership
│   │   └─ community/# Post, Comment, Like
│   ├─ recommendation/
│   ├─ admin/
│   └─ api/         # REST 컨트롤러
├─ src/main/resources/
│   ├─ application.yml
│   └─ static/ (X)
└─ build.gradle
```

## 5) 데이터 모델 가이드(핵심 컬럼)
> 실제 DDL은 /docs/schema.sql
### (1) User
- `id (PK, CHAR(36))`, `email (UNIQUE)`, `password`, `nickname`, `status (ACTIVE/DELETED)`, `role (USER/ADMIN)`
- `provider (LOCAL/GOOGLE/KAKAO)`, `provider_id`
- `preference_keywords (JSON)`, `penalty_count (int, default 0)`
- `created_at`, `updated_at`, `deleted_at (nullable)`
### (2) Auth & Token
- JWT Access/Refresh는 서버 저장 없이 stateless가 원칙
- 선택) Refresh 블랙리스트/세션 추적은 Redis 사용: `refresh:{userId} -> token`
### (3) Cafe / Tag / Wishlist
- `cafe` : `id`, `name`, `address`, `latitude`, `longitude`, `open_hours`, `phone`, `menu(JSON)`, `photos(JSON)`, `view_count`, `created_at`
- `tag` : `id`, `name`
- `cafe_tag` : `(cafe_id, tag_id)` 복합 PK
- `withlist` : `(user_id, cafe_id)` + `wishlist_type (BASIC/SPECIAL)`, `created_at`
### (4) Review
-  `id`, `user_id`, `cafe_id`, `rating(int 1~5)`, `content`, `images(JSON)`, `created_at`, `updated_at`, `status(ACTIVE/DELETED)`
### (5) Chat
- `chatroom` : `id`, `cafe_id`, `name`, `max_capacity`, `created_at`
- `chat_membership` : `(chatroom_id, user_id)` 복합 PK
- `chat_message` : `id`, `chatroom_id`, `user_id`, `type(TEXT/IMAGE)`, `payload`, `created_at`
### (6) Community
- `post` : `id`, `user_id`, `title`, `content`, `created_at`, `updated_at`, `status`
- `comment` : `id`, `post_id`, `user_id`, `parent_id(nullable)`, `content`, `created_at`
- `post_like` : `(post_id, user_id)` 복합 PK

## 6) 인증/인가 설계
### (1) 로컬 로그인
```sql
POST /api/auth/login
-> { email, password }
<- { accessToken, refreshToken, user }
```
### (2) 소셜 로그인(OAuth2)
- 프론트에서 `/oauth2/authorization/{provider}`로 리다이렉트
- 콜백: `/login/oauth2/code/{provider}`-> 서버에서 토큰 교환 -> User 매핑(존재하지 않으면 가입) -> JWT 발급
### (3) 토큰 갱신
```sql
POST /api/auth/refresh
-> { refreshToken }
<- { accessToken, refreshToken }
```
### (4) 로그아웃
- 클라이언트 토큰 폐기
- 서버는 선택적으로 `refresh:{userId}` 삭제(또는 해당 RT 블랙리스트 등록)
### (5) 회원 탈퇴(Soft Delete)
- `status=DELETED`, `deleted_at` 기록
- 리뷰/게시글 등은 남기되 UI에서 "탈퇴 회원"으로 마스킹
### (7) 주요 API 요약
> 응답 래핑: `{ "success": true|false, "data": ..., "error": {...} }` 권장
- Auth
  - `POST /api/auth/signup` (이메일 인증/임시비번 플로우 포함)
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - `POST /api/auth/logout`
  - `POST /api/auth/password/reset` (비로그인 상태, 이메일 발송)
  - `PUT /api/auth/password` (로그인 상태에서 변경)
- User/Profile
  - `GET /api/users/me`
  - `PUT /api/users/me` (닉네임·선호 키워드)
  - `DELETE /api/users/me` (Soft Delete)
- Cafe
  - `GET /api/cafes?query=&region=&tags=&sort=VIEW|WISHLIST|NEW`
  - `GET /api/cafes/nearby?latitude=&longitude=&radius=`
  - `GET /api/cafes/{id}`
  - `GET /api/cafes/{id}/related` (태그 기반 추천)
- Wishlist
  - `GET /api/users/me/wishlist`
  - `PUT /api/wishlist/{cafeId}` (추가/수정: `wishlist_type`)
  - `DELETE /api/wishlist/{cafeId}`
- Review
  - `GET /api/cafes/{id}/reviews?sort=LATEST|RATING`
  - `POST /api/cafes/{id}/reviews` (이미지 S3 pre-signed URL 발급 후 업로드 권장)
  - `PUT /api/reviews/{reviewId}`
  - `DELETE /api/reviews/{reviewId}`
- Chat(WebSocket/STOMP)
  - 연결 : `ws://host/ws`
  - 구독 : `/topic/rooms/{roomId}`
  - 발행 : `/app/rooms/{roomId}/send`
  - REST :
    - `GET /api/chats/rooms?cafeId=`
    - `POST /api/chats/rooms` (관리자/자동 생성 정책)
    - `POST /api/chats/rooms/{id}/join`
    - `POST /api/chats/rooms/{id}/leave`
- Community
  - `GET /api/posts`
  - `POST /api/posts`
  - `GET /api/posts/{id}`
  - `POST /api/posts/{id}/comments`
  - `POST /api/posts/{id}/like`
- Admin
  - `POST /api/admin/cafes`
  - `PUT /api/admin/cafes/{id}`
  - `DELETE /api/admin/cafes/{id}`
  - `GET /api/admin/reports` (신고)
  - `POST /api/admin/reviews/{id}/moderate`
  - `POST /api/admin/users/{id}/penalty`
  - `POST /api/admin/users/{id}/suspend` (정지)
#### 샘플 요청
```bash
# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "a@b.com", "password": "pass" }'

# 카페 검색
curl "http://localhost:8080/api/cafes?query=성수&tags=디저트,루프탑&sort=VIEW" \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### (8) 추천 로직(초기 버전)
- 룰 기반:
  - (1) 사용자 `preference_keywords`와 카페 `tag` 교집합 점수
  - (2) 히스토리(방문·리뷰·북마크) 가중치
  - (3) 기본 인기 가점(조회수·찜수)

### (9) 파일 업로드(S3)
- 서버에서 Pre-signed URL 발급 -> 클라이언트가 S3 직접 업로드 -> 업로드 결과 경로를 리뷰 생성 시 전달
- 장점 : 서버 부하 ↓, 속도 ↑, 보안 token 만료 관리 쉬움

### (10) 보안 정책
- 비밀번호 BCrypt 해시
- JWT 만료 짧게(Access 30분) + Refresh 14일
- 관리자 API는 `ROLE_ADMIN` 필수
- 속도 제한 : IP/계정별 로그인 시도(예: Redis + 단일 키 윈도우)
- CORS : FE 도메인만 허용, 쿠키 기반이 아니므로 헤더 Bearer 사용
- 입력 검증 : `@Valid`, 커스텀 Validator(별점 1~5 등)

### (11) 에러·응답 규약
```json
{
  "success": false,
  "error": {
    "code": "AUTH_INVALID_CREDENTIAL",
    "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
    "traceId": "c5f7..."
  }
}
```
- 공통 코드 예시 : `AUTH_*`, `VALIDATION_*`, `NOT_FOUND`, `FORBIDDEN`, `CONFLICT`, `RATE_LIMITED`, `S3_UPLOAD_FAILED`, `OAUTH2_CALLBACK_ERROR`

### (12) 문서화
- Swagger UI: `/swagger-ui/index.html`
- API 명세서 원칙
  - 요청·응답 스키마, 예시, 에러 코드 포함
  - 인증 필요 여부 및 권한 명시

### (13) 운영
- 로그 : JSON 포맷(요청ID/사용자ID/URI/응답시간)
- Actuator : `/actuator/health`, `/metrics`, `loggers`
- CI : PR 시 빌드·테스트·정적 분석, main 머지 시 Docker 이미지 빌드/푸시
- CD : (선택) GitHub Actions -> EC2 배포, Blue/Green 또는 Rolling

### (14) 개발 규칙
- 브랜치 전략
  - `main`(배포), `dev`(통합), `feat/*`, `fix/*`, `chore/*`
- 커밋 컨벤션(Conventional Commits)
  - `feat:`, `fix:`, `refactor:`, `test:`, `chore:` ...
- 코드 스타일
  - Controller-Service-Repository 분리
  - DTO/Entity 분리
  - 요청 검증은 Controller 레벨에서
  - 서비스 트랜잭션 경계는 Service

### (15) 프런트엔드 연동 포인트
- CORS 도메인·헤더 합의
- OAuth2 리다이렉트 URI 공유
- 이미지 업로드: Pre-singed URL 워크플로
- WebSocket 엔드포인트(`/ws`), STOMP topic 경로 합의
- 검색·필터 파라미터 네이밍 스펙 고정

### (15) 로드맵
- ai 요약(네이버 리뷰 api or OpenAI) 실제 연동 샘플 엔드포인트 제공
- 추천 엔진 고도화(협업 필터링), A/B 테스트
- Geo-Index(포스트GIS/ES)로 주변 검색 최적화
- 알림(푸시/이메일) 구독

---
### 부록: 최소 DDL 스케치(참고용)
> 실제 운영은 Flyway 마이그레이션으로 관리하세요
```sql
-- USER
CREATE TABLE user (
    user_id CHAR(36) primary key,   -- CHAR(36) 문자열을 그대로 저장(32자리+하이픈4개=총36자)
    email VARCHAR(255) UNIQUE not null,
    password VARCHAR(255),
    nickname VARCHAR(50),
    profile_image JSON,
    status ENUM('ACTIVE', 'SUSPENDED', 'DELETED') not null DEFAULT 'ACTIVE',
    role ENUM('USER', 'ADMIN') not null DEFAULT 'USER',
    provider ENUM('LOCAL', 'KAKAO', 'GOOGLE', 'NAVER') not null DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    preference_keywords JSON,
    refresh_token VARCHAR(512),
    penalty_count INT not null DEFAULT 0,
    created_at TIMESTAMP not null DEFAULT now(),
    updated_at TIMESTAMP not null DEFAULT now(),
    deleted_at TIMESTAMP
);

-- CAFE
CREATE TABLE cafe (
    id INT primary key AUTO_INCREMENT,
    name VARCHAR(200) not null,
    address VARCHAR(300),
    latitude double,
    longitude double,
    open_hours JSON,
    phone VARCHAR(50),
    menu JSON,
    photos JSON,
    view_count INT not null default 0,
    created_at TIMESTAMP not null DEFAULT now()
);

-- TAG
CREATE TABLE tag (
    id INT primary key AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE not null
);

CREATE TABLE cafe_tag (
    cafe_id INT not null,
    tag_id INT not null,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE,
    PRIMARY KEY (cafe_id, tag_id)
);

-- WISHLIST
CREATE TABLE wishlist (
    user_id CHAR(36) not null,
    cafe_id INT not null,
    withlist_type VARCHAR(16) not null DEFAULT 'BASIC',
    created_at TIMESTAMP not null DEFAULT now(),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (cafe_id) REFERENCES cafe(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, cafe_id)
);

-- REVIEW
CREATE TABLE review (
    id INT primary key AUTO_INCREMENT,
    user_id CHAR(36) not null,
    cafe_id INT not null,
    rating INT not null check (rating BETWEETn 1 AND 5),
    content TEXT,
    images JSON,
    status VARCHAR(16) not null DEFAULT 'ACTIVE',
    created_at TIMESTAMP not null DEFAULT now(),
    updated_at TIMESTAMP not null DEFAULT now(),
    FOREIGN KEY user_id REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY cafe_id REFERENCES cafe(id) ON DELETE CASCADE
);

-- CHAT
CREATE TABLE chatroom (
    id INT primary key AUTO_INCREMENT,
    cafe_id INT,
    name VARCHAR(100) not null,
    max_capacity INT not null default 100,
    created_at TIMESTAMP not null DEFAULT now(),
    FOREIGN KEY cafe_id REFERENCES cafe(id) ON DELETE SET NULL
);

CREATE TABLE chat_message (
    id INT primary key AUTO_INCREMENT,
    chatroom_id INT not null,
    user_id CHAR(36) not null,
    type VARCHAR(16) not null,
    payload TEXT not null,
    created_at TIMESTAMP not null DEFAULT now(),
    FOREIGN KEY chatroom_id REFERENCES chatroom(id) ON DELETE CASCADE,
    FOREIGN KEY user_id REFERENCES user(id) ON DELETE SET NULL
);

-- COMMUNITY
CREATE TABLE post (
    id INT primary key AUTO_INCREMENT,
    user_id CHAR(36) not null,
    title VARCHAR(200) not null,
    content TEXT not null,
    status VARCHAR(16) not null DEFAULT 'ACTIVE',
    created_at TIMESTAMP not null DEFAULT now(),
    updated_at TIMESTAMP not null DEFAULT now(),
    FOREIGN KEY user_id REFERENCES user(id) ON DELETE SET NULL
);

CREATE TABLE comment (
    id INT primary key AUTO_INCREMENT,
    post_id INT not null,
    user_id CHAR(36) not null,
    parent_id INT,
    content TEXT not null,
    created_at TIMESTAMP not null DEFAULT now(),
    FOREIGN KEY post_id REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY user_id REFERENCES user(id) ON DELETE SET NULL,
    FOREIGN KEY parent_id REFERENCES comment(id) ON DELETE CASCADE
);

CREATE TABLE post_like (
    post_id INT not null,
    user_id CHAR(36) not null,
    primary key (post_id, user_id),
    FOREIGN KEY post_id REFERENCES post(id) ON DELETE CASCADE,
    FOREIGN KEY user_id REFERENCES user(id) ON DELETE CASCADE
);
```