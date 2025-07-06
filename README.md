# Spring Boot Demo Project

Spring Boot + Kotlin + Java + JWT + QueryDSL + Swagger + Docker 데모 프로젝트입니다.

## 기술 스택

- **Spring Boot 3.5.3**
- **Kotlin & Java** (혼용 가능)
- **Spring Security + JWT**
- **Spring Data JPA**
- **QueryDSL**
- **H2 Database** (개발용) / **MySQL** (Docker)
- **Swagger/OpenAPI 3**
- **Docker & Docker Compose**

## 프로젝트 구조

```
src/main/
├── kotlin/com/example/demo/
│   ├── config/           # 설정 클래스들
│   ├── controller/       # REST API 컨트롤러
│   ├── domain/          # 엔티티
│   ├── dto/             # 데이터 전송 객체
│   ├── exception/       # 예외 처리
│   ├── repository/      # 데이터 접근 계층
│   ├── security/        # JWT 보안 관련
│   └── service/         # 비즈니스 로직
└── java/com/example/demo/java/
    ├── JavaUserController.java  # Java 예시 컨트롤러
    └── JavaUserService.java     # Java 예시 서비스
```

## 주요 기능

### 1. 사용자 관리
- 회원가입/로그인
- JWT 토큰 인증
- 사용자 조회 및 검색

### 2. 데이터 접근
- **JPA Repository**: 기본 CRUD
- **QueryDSL**: 복잡한 쿼리
- **Raw Query**: 네이티브 SQL
- **JPQL**: 객체 지향 쿼리

### 3. 보안
- Spring Security
- JWT 토큰 기반 인증
- 역할 기반 접근 제어 (RBAC)

### 4. API 문서화
- Swagger/OpenAPI 3
- JWT 인증 지원

### 5. 예외 처리
- Global Exception Handler
- 표준화된 에러 응답

## 실행 방법

### 1. 로컬 개발 환경

```bash
# 프로젝트 클론
git clone <repository-url>
cd springboot-demo

# 애플리케이션 실행
./gradlew bootRun
```

### 2. Docker 환경 (외부 MySQL 서버 사용)

#### 환경별 실행 방법

**개발 환경 실행 (외부 MySQL 서버 사용):**
```bash
# 1. 환경 변수 설정
cp env.example .env
# .env 파일을 편집하여 개발용 외부 MySQL 서버 정보 입력

# 2. 개발 환경 실행
./run-dev.sh
```

**운영 환경 실행:**
```bash
# 1. 환경 변수 설정
cp env.example .env
# .env 파일을 편집하여 운영용 외부 MySQL 서버 정보 입력

# 2. 운영 환경 실행
./run-prod.sh
```

**수동 실행:**
```bash
# 개발 환경 (외부 MySQL 서버 사용)
ENVIRONMENT=dev docker-compose -f docker-compose.dev.yml up --build

# 운영 환경
ENVIRONMENT=prod docker-compose up --build -d
```

**개발용 MySQL 관리:**
```bash
# MySQL 컨테이너만 시작
./dev-mysql.sh start

# MySQL 컨테이너만 중지
./dev-mysql.sh stop

# MySQL에 직접 연결
./dev-mysql.sh connect

# MySQL 데이터 초기화
./dev-mysql.sh reset
```

### 3. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 다음 정보를 설정하세요:

```bash
# ========================================
# 환경 설정 (ENVIRONMENT)
# ========================================
# 개발 환경: dev
# 운영 환경: prod
ENVIRONMENT=dev

# ========================================
# 개발 환경 설정 (Development)
# ========================================
# 개발용 데이터베이스
DEV_SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/demo_dev?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DEV_SPRING_DATASOURCE_USERNAME=demo_dev
DEV_SPRING_DATASOURCE_PASSWORD=demo_dev_password

# 개발용 JWT 설정
DEV_JWT_SECRET=dev-secret-key-change-in-production
DEV_JWT_EXPIRATION=86400000

# ========================================
# 운영 환경 설정 (Production)
# ========================================
# 운영용 데이터베이스
PROD_SPRING_DATASOURCE_URL=jdbc:mysql://your-prod-mysql-host:3306/demo_prod?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
PROD_SPRING_DATASOURCE_USERNAME=demo_prod
PROD_SPRING_DATASOURCE_PASSWORD=your-strong-production-password

# 운영용 JWT 설정
PROD_JWT_SECRET=your-very-long-and-secure-production-secret-key
PROD_JWT_EXPIRATION=3600000

# ========================================
# 공통 설정 (Common)
# ========================================
# 애플리케이션 포트
APP_PORT=8080

# 로그 레벨
LOG_LEVEL=INFO
```

**⚠️ 보안 주의사항:**
- `.env` 파일은 절대 Git에 커밋하지 마세요
- 실제 운영 환경의 데이터베이스 정보는 안전하게 관리하세요
- `env.example` 파일은 템플릿용이므로 Git에 포함됩니다

### 4. 환경별 설정 차이점

| 설정 항목 | 개발 환경 (dev) | 운영 환경 (prod) |
|-----------|----------------|------------------|
| 데이터베이스 | MySQL (외부 서버) | MySQL (외부 서버) |
| JPA DDL | create-drop | validate |
| SQL 로깅 | 활성화 | 비활성화 |
| H2 Console | 비활성화 | 비활성화 |
| Swagger UI | 활성화 | 선택적 비활성화 |
| 로그 레벨 | DEBUG | INFO |
| JWT 만료시간 | 24시간 | 1시간 |
| SSL | 선택적 | 필수 |

### 5. 외부 MySQL 서버 설정

**개발용 MySQL 서버 정보:**
- **호스트**: `your-dev-mysql-host:3306`
- **데이터베이스**: `demo_dev`
- **사용자**: `your-dev-username`
- **비밀번호**: `your-dev-password`

**운영용 MySQL 서버 정보:**
- **호스트**: `your-prod-mysql-host:3306`
- **데이터베이스**: `demo_prod`
- **사용자**: `demo_prod`
- **비밀번호**: `your-strong-production-password`

**환경 변수 설정 예시:**
```bash
# 개발용 MySQL 서버
DEV_SPRING_DATASOURCE_URL=jdbc:mysql://dev-mysql.company.com:3306/demo_dev
DEV_SPRING_DATASOURCE_USERNAME=dev_user
DEV_SPRING_DATASOURCE_PASSWORD=dev_password

# 운영용 MySQL 서버
PROD_SPRING_DATASOURCE_URL=jdbc:mysql://prod-mysql.company.com:3306/demo_prod
PROD_SPRING_DATASOURCE_USERNAME=prod_user
PROD_SPRING_DATASOURCE_PASSWORD=prod_password
```

## API 엔드포인트

### 인증 API
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인

### 사용자 API (Kotlin)
- `GET /api/users` - 전체 사용자 조회 (ADMIN)
- `GET /api/users/{id}` - 특정 사용자 조회
- `GET /api/users/role/{role}` - 역할별 사용자 조회 (ADMIN)
- `GET /api/users/search?email={email}` - 이메일로 검색 (ADMIN)
- `GET /api/users/search/complex?name={name}&role={role}` - 복합 조건 검색 (ADMIN)

### Java 예시 API
- `POST /api/java/users/signup` - Java 회원가입
- `GET /api/java/users` - Java 전체 사용자 조회 (ADMIN)
- `GET /api/java/users/{id}` - Java 특정 사용자 조회
- `GET /api/java/users/role/{role}` - Java 역할별 사용자 조회 (ADMIN)

## 접근 URL

- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **H2 Console**: http://localhost:8080/h2-console

## 데이터베이스 접속 정보 (H2)

- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (비어있음)

## JWT 토큰 사용법

1. **로그인**하여 JWT 토큰 획득
2. **Authorization 헤더**에 `Bearer {token}` 형식으로 추가
3. 보호된 API 호출

## 예시 요청

### 회원가입
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "name": "홍길동"
  }'
```

### 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### 보호된 API 호출
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer {your-jwt-token}"
```

## 개발 가이드

### Kotlin vs Java
- **Kotlin**: 메인 비즈니스 로직, 최신 기능 활용
- **Java**: 레거시 시스템 연동, 팀 내 Java 개발자 지원

### QueryDSL 사용법
```kotlin
// 복잡한 조건 쿼리
val users = queryFactory
    .selectFrom(user)
    .where(
        user.name.containsIgnoreCase(name).and(user.role.eq(role))
    )
    .fetch()
```

### Raw Query 사용법
```kotlin
@Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
fun findByEmailRawQuery(@Param("email") email: String): Optional<User>
```

## 빌드 및 배포

```bash
# JAR 파일 빌드
./gradlew build

# Docker 이미지 빌드
docker build -t springboot-demo .

# Docker Compose 실행
docker-compose up --build
```

## 주의사항

1. **JWT Secret**: 프로덕션에서는 강력한 시크릿 키 사용
2. **데이터베이스**: 프로덕션에서는 영구 데이터베이스 사용
3. **보안**: CORS, HTTPS 등 추가 보안 설정 필요
4. **로깅**: 프로덕션에서는 적절한 로그 레벨 설정

## 라이센스

MIT License 