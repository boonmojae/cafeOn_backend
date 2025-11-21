# ☕ CafeOn
취향 기반 카페 탐색 & 실시간 커뮤니티 플랫폼

> “지금 나에게 맞는 카페를 한 번에 찾고, 바로 소통까지 할 수 없을까?”

포털 검색, 블로그, 지도 앱, 리뷰 앱을 동시에 켜놓고 카페를 비교하는 건 생각보다 번거롭다.  
정보는 여기저기 흩어져 있고, 광고성 글이나 편향된 리뷰 때문에 **직접 가보기 전까지는 확신하기 어려운 경우**도 많다.

**CafeOn**은 카페 정보 · 리뷰 · 실시간 소통을 한 곳에 모아,  
“**지금 나에게 맞는 카페를 빠르게 찾고, 바로 소통까지 할 수 있는 공간**”을 목표로 만든 서비스이다.

- 태그 · 필터 · 지도 기반 검색으로 목적에 맞는 카페를 쉽게 찾고,
- 리뷰 · 게시글 · 댓글 · 실시간 채팅을 통해 같은 카페를 이용하는 사람들끼리 자연스럽게 연결되도록 설계했다.
- AWS 기반 인프라와 JWT 인증, WebSocket 실시간 채팅으로 **안정적인 서비스와 빠른 피드백 경험**을 제공한다.

---

## 🗓️ 프로젝트 개요

- **프로젝트명**: CafeOn
- **개발 기간**: 2024.07 ~ 2024.11
- **참여 인원**: 5명

### 🔍 주요 기능

- 🔐 사용자 인증 (회원가입, 로그인, JWT 기반 인증/인가)
- 📍 카페 검색 및 상세 조회 (지도/태그/필터 기반)
- ✍️ 리뷰 & 게시글 & 댓글 기능
- 👍 게시글/댓글 좋아요 기능
- ⚡💬 실시간 채팅 (카페/모임 단위 채팅방)
- ⭐ 찜(즐겨찾기) 및 마이페이지
- 🛠️ 관리자 페이지 (카페/태그/회원 관리)

---

## 🛠 기술 스택

### Frontend

![Next.js](https://img.shields.io/badge/Next.js_15-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React_19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/TailwindCSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white)
![STOMP.js](https://img.shields.io/badge/@stomp/stompjs-231F20?style=for-the-badge)
![React Icons](https://img.shields.io/badge/React%20Icons-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Google Maps API](https://img.shields.io/badge/Google%20Maps%20API-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white)
![ESLint](https://img.shields.io/badge/ESLint-4B32C3?style=for-the-badge&logo=eslint&logoColor=white)
![PostCSS](https://img.shields.io/badge/PostCSS-DD3A0A?style=for-the-badge&logo=postcss&logoColor=white)

### Backend

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JPA](https://img.shields.io/badge/JPA%20(Hibernate)-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-FF6F00?style=for-the-badge&logo=socketdotio&logoColor=white)
![STOMP](https://img.shields.io/badge/STOMP-CC0000?style=for-the-badge&logo=activemq&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-000000?style=for-the-badge)

### Infra & Tools

![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?style=for-the-badge&logo=amazon-ec2&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS%20RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)

---

## 👥 팀원 소개

| 역할     | 이름   | 담당                                                                                                                                                                                                                                                                                                                                                                   |
|----------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Backend  | 박민재 | **Post** – 게시글 조회/검색, 게시글 생성(텍스트/이미지), 수정, 삭제, 좋아요/취소<br/>**Comment** – 댓글 조회/생성/수정/삭제, 댓글 좋아요/취소<br/>**Chat** – 1:1 채팅방 생성, 단체 채팅방 생성, 텍스트/이미지 채팅, 채팅방 나가기<br/>**Review** – 리뷰 작성/수정/삭제/조회(텍스트/이미지)<br/>**Report** – 게시글/댓글/리뷰 신고<br/>**Infra & DB** – DB 모델링, AWS(EC2/RDS/S3) 기반 배포 및 운영 |
| Backend  | 김도이 | **User** – 회원가입, 로그인(JWT Access/Refresh), 로그아웃(Refresh Token 무효화), 로그인 상태 비밀번호 변경, 비로그인 상태 임시 비밀번호 발급 메일링<br/>**Cafe** – 카카오맵 REST API + 파이썬으로 카페 데이터 수집, Selenium으로 영업시간 크롤링 및 패턴 정규화, 카페 후기 데이터 수집, 카페 검색/전체 조회, AI API로 후기 요약/태그 추출<br/>**Tag** – 카페/태그 매핑                       |
| Backend  | 김가연 | **User** – 유저 프로필 조회/수정, 유저 탈퇴<br/>**Question** – 문의 목록 조회/검색, 문의 작성(공개/비공개), 수정, 삭제<br/>**Answer** – 문의 답변 작성<br/>**Wishlist** – 찜 생성/삭제, 내가 찜한 카페들 조회<br/>**Admin** – 전체 유저 조회, 신고 목록 조회, 유저 패널티 추가                                                                                           |
| Frontend | 최아름 | **공통(인증)** – 로그인/회원가입 등 인증 플로우 UI<br/>**홈페이지/검색결과/지도 페이지** – 메인 홈, 카페 검색 결과, 지도 연동 화면<br/>**카페 상세 페이지** – 카페 상세 정보, 리뷰/게시글/댓글 UI<br/>**관리자 페이지** – 관리자용 웹 화면 및 기능 UI                                                                                                                |
| Frontend | 이하민 | **채팅방** – 실시간 채팅방 UI, 메시지 리스트/입력 인터랙션<br/>**마이페이지** – 내 정보, 내가 쓴 글·리뷰·댓글·찜 목록 화면<br/>**커뮤니티** – 게시글 피드, 작성/수정 UI<br/>**알림/문의하기 페이지** – 알림 리스트, 문의 작성/조회 화면                                                                                                             |

---

## 🚀 주요 기능

### 1. 🔐 사용자 인증 & 권한 관리

- 이메일 기반 회원가입 / 로그인
- Spring Security + JWT를 활용한 토큰 기반 인증
- Access / Refresh Token 구조 및 재발급
- 일반 사용자 / 관리자에 따른 권한 분리

### 2. 📍 카페 검색 & 상세 조회

- 태그, 위치, 필터 기반 카페 목록 조회
- 카페 상세 페이지에서
    - 기본 정보 (주소, 전화번호, 영업시간 등)
    - 사진, 리뷰, 게시글, 지도 위치
- 인기 카페 / 랜덤 카페 등 추천성 조회 기능

### 3. ✍️ 콘텐츠(리뷰 / 게시글 / 이미지) 기능

- 리뷰(Review)
    - 카페에 대한 리뷰 작성 / 수정 / 삭제
    - 특정 카페와 사용자에 연관되어, 카페 상세 페이지와 마이페이지에서 조회 가능
    - 텍스트 + 이미지 기반 리뷰 작성 지원 (S3에 저장된 이미지 참조)

- 게시글(Post)
    - 커뮤니티 게시글 작성 / 수정 / 삭제
    - 카페와는 분리된 커뮤니티 도메인으로, 사용자와만 연관
    - `type` 필드로 **GENERAL / INFO / QUESTION** 등 게시글 성격 구분

- 이미지(Image)
    - 이미지 엔티티/테이블을 게시글, 리뷰, 채팅 메시지에서 **공용으로 사용**하도록 분리 설계
    - 실제 파일은 AWS S3에 업로드하고, DB에는 이미지 메타데이터 및 URL만 저장


### 4. 💬 댓글 & 좋아요 기능

- 게시글에 댓글 작성 / 수정 / 삭제
- 상위 댓글 ID를 활용한 댓글 / 대댓글 구조 지원
- 댓글 개수 집계 및 정렬 (최신순/등록순)
- 댓글은 게시글(Post)에만 연결
- 게시글/댓글에 대해 좋아요/취소 기능 제공
- 사용자·대상 콘텐츠 기준으로 중복 좋아요를 방지하는 도메인 설계
- 좋아요 수를 별도 컬럼으로 관리하여 목록 정렬 및 UI 표시 성능 최적화

### 5. ⭐ 찜(즐겨찾기) & 마이페이지

- 관심 있는 카페 찜 등록 / 해제
- 마이페이지에서
    - 내가 찜한 카페
    - 내가 작성한 리뷰/게시글/댓글 목록 조회
- 개인 프로필 정보(닉네임 등) 수정 기능

### 6. ⚡💬 실시간 채팅 (WebSocket + STOMP)

- 카페별 / 1:1 채팅방 생성 및 참여
- WebSocket + STOMP 기반 실시간 메시지 전송
- 입장/퇴장 알림, 시스템 메시지 처리
- 읽지 않은 메시지 수, 최근 메시지 기준 정렬 등 UX 고려
- 채팅방에 새 메시지가 도착하면 해당 방의 안 읽은 메시지를 기반으로 개인 알림을 생성 -> 
  사용자가 채팅방에 입장하는 순간 알림 목록에서도 자동으로 제거되도록 구현

### 7. 🛠️ 관리자 페이지

- 관리자 전용 계정으로 로그인
- 카페 정보 등록 / 수정 / 비활성화
- 태그 관리 (카페-태그 매핑)
- 신고된 게시글/리뷰 및 사용자 관리
- 서비스 품질 및 커뮤니티 분위기 유지를 위한 최소한의 운영 도구 제공  
