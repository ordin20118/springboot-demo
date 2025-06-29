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

### 2. Docker 환경

```bash
# Docker Compose로 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d --build
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