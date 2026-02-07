# Nect-Backend

<div align="center">
  <img src="docs/logo.png" width="300" alt="Nect 로고" />
  <h3>Nect Backend</h3>
  <p>Spring Boot 멀티 모듈 기반 백엔드 프로젝트</p>
</div>

<!-- 방문 카운터를 쓰고 싶다면 아래 예시를 참고해서 교체하세요 -->
<!-- <img src="COUNTER_IMAGE_URL" /> -->

## 01. 프로젝트에 대한 정보

### (1) 프로젝트 제목

- Nect-Backend

### (2) 프로젝트 로고나 이미지

- `docs/logo.png` (필요 시 교체)

### (3) Repository 방문 횟수

- 선택 사항 (필요 시 카운터 이미지 추가)

### (4) 프로젝트 정보

- 진행 단체/목적: TODO
- 개발 기간: TODO (예: 2026.01.01 ~ 2026.02.28)

### (5) 배포 주소

- 웹/프론트: TODO
- 백엔드(API): TODO

### (6) 팀 소개

- TODO: 팀원 이름 / 역할 / GitHub / 소속

### (7) 프로젝트 소개

Nect는 사용자 경험을 개선하는 서비스를 목표로 하는 백엔드 프로젝트입니다.  
API 서버와 스케줄러 서버를 분리하고, 공용 모듈을 통해 코드 재사용성과 유지보수성을 높였습니다.  
OAuth2 로그인과 JWT 인증을 지원하며, Redis를 활용한 캐싱/세션 관리가 가능합니다.  
외부 연동을 위한 클라이언트 모듈을 별도 분리하여 확장성을 확보했습니다.  
실행 환경에 따라 H2(로컬) 또는 PostgreSQL(운영)로 유연하게 전환할 수 있습니다.

## 02. 시작 가이드

### (1) 요구 사항

- JDK 21
- (선택) Docker / Docker Compose

### (2) 설치 및 실행

```bash
# API 서버 실행
./gradlew :nect-api:bootRun

# Scheduler 실행
./gradlew :nect-scheduler:bootRun
```

기본 포트는 `8080` 입니다.

#### 환경 변수 (.env 예시)

`.env` 파일을 루트에 두면 자동 로딩됩니다.

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/nect
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_DRIVER=org.postgresql.Driver
JPA_DIALECT=org.hibernate.dialect.PostgreSQLDialect
JPA_DDL_AUTO=validate

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=change-me
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=86400000

# OAuth2
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_REDIRECT_URI=
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=
OAUTH2_REDIRECT_URI=http://localhost:3000/auth/callback

# R2 (S3 compatible)
R2_ACCESS_KEY=
R2_SECRET_KEY=
R2_REGION=us-east-1
R2_END_POINT=
BUCKET_NAME=

# App
AUTH_KEY=

# OpenAI
OPENAI_API_KEY=
OPENAI_BASE_URL=https://api.openai.com
OPENAI_MODEL=gpt-4o-mini
OPENAI_FALLBACK_MODEL=gpt-4.1
```

## 03. 기술 스택

- Java 21
- Spring Boot 3.5.x
- Gradle (멀티 모듈)
- JPA / QueryDSL
- Redis
- OAuth2 (Google/Kakao), JWT
- OpenAI API (클라이언트 모듈)

## 04. 화면 구성 / API 주소

- API 문서: TODO (예: Swagger/OpenAPI 경로)

## 05. 주요 기능

- OAuth2 로그인 및 JWT 인증
- Redis 기반 캐싱/세션 관리
- 배치/스케줄러 작업 분리 운영
- 외부 API 연동(클라이언트 모듈)

## 06. 아키텍처

### 모듈 구성

- `nect-api`: 메인 API 서버
- `nect-scheduler`: 배치/스케줄러 서버
- `nect-core`: JPA/도메인 공용 모듈
- `nect-client`: 외부 연동/클라이언트 공용 모듈

### 디렉토리 구조

```text
.
├── nect-api
│   ├── build.gradle
│   └── src
├── nect-scheduler
│   ├── build.gradle
│   └── src
├── nect-core
│   ├── build.gradle
│   └── src
├── nect-client
│   ├── build.gradle
│   └── src
├── gradle
├── build.gradle
├── settings.gradle
├── docker-compose.yml
├── Dockerfile
└── nginx.conf
```

## 07. 기타

- 기본값은 H2 in-memory DB로 동작합니다. (설정 미지정 시)
- `nect-api` 모듈에서 OpenAPI 문서를 생성합니다.
