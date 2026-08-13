# 🔐 Spring Boot Security & JPA 소셜 로그인 실습 프로젝트

본 프로젝트는 Spring Boot와 Spring Security, JPA(PostgreSQL)를 연동하여 데이터베이스 기반의 회원가입/로그인, 댓글 시스템(CRUD 및 쿼리 최적화), 관리자 권한 제어, 그리고 카카오 소셜 로그인까지 단계별로 구축한 실습 저장소입니다.

---

## 🛠️ 실습 환경 설정

### 1. 기술 스택
* **Language/Framework**: Java 17, Spring Boot 4.1.0, Gradle
* **Database**: PostgreSQL (Neon Serverless DB)
* **Security**: Spring Security 6 (OAuth2 Client)
* **Template Engine**: Thymeleaf (Thymeleaf Extras SpringSecurity6)

### 2. 로컬 환경 설정 (`.env.dev`)
애플리케이션 실행을 위해 프로젝트 루트 경로에 `.env.dev` 파일을 생성하고 아래 환경 변수들을 알맞게 기입해야 합니다. (이 파일은 `.gitignore`에 의해 커밋에서 제외됩니다.)

```properties
# 데이터베이스 설정 (PostgreSQL / Neon)
DB_HOST=your-neon-db-host
DB_PORT=5432
DB_NAME=neondb
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password

# 카카오 소셜 로그인 API 설정 (REST API Key & Client Secret)
KAKAO_CLIENT_ID=your-kakao-rest-api-key
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

---

## 📝 주요 실습 단계 및 핵심 개념 정리

### 1️⃣ DB 환경설정 및 메인 화면 구성
* **다중 프로파일 설정**: `dev, db, kakao` 프로파일을 분리하여 환경 변수를 모듈화하고 로컬 개발 단계에서는 `.env.dev` 파일을 import하여 민감한 정보를 로드하도록 구현했습니다.
* **JPA DDL Auto**: `update` 옵션을 활성화하여 엔티티 모델 변경 사항이 데이터베이스 스키마에 자동으로 반영되도록 구성했습니다.

### 2️⃣ Spring Security & DB 기반 회원가입/로그인
* **비밀번호 암호화**: BCrypt, SCrypt뿐만 아니라 현대적인 해시 알고리즘인 **Argon2**(`Argon2PasswordEncoder`)를 포함한 `DelegatingPasswordEncoder`를 구축하여 비밀번호를 안전하게 암호화 저장했습니다.
* **CustomUserDetails**: Spring Security가 사용하는 인증 객체인 `UserDetails` 인터페이스를 직접 구현하여 로그인 성공 시 사용자 고유의 식별 정보(PK ID, 권한 목록) 등을 편리하게 꺼내 쓸 수 있도록 확장했습니다.
* **CustomUserDetailsService**: DB의 `user_account` 테이블에서 입력받은 `username`을 조회하여 시큐리티 인증 객체로 변환해주는 비즈니스 로직을 구축했습니다.
* **Thymeleaf Security**: `thymeleaf-extras-springsecurity6`를 연동해 HTML 화면 내에서 `sec:authorize="isAuthenticated()"` 및 `sec:authentication="name"` 등 시큐리티 표현식을 활용한 UI 동적 제어를 실습했습니다.

### 3️⃣ JPA 댓글 게시판 CRUD & N+1 성능 최적화
* **연관 관계 매핑**: 댓글 엔티티(`CommentEntity`)를 생성하고 회원 엔티티(`UserAccountEntity`)와 `ManyToOne` 다대일 단방향 관계를 매핑했습니다.
* **N+1 쿼리 문제 해결**:
  * 댓글 목록을 조회할 때 작성자 정보(`UserAccountEntity`)를 조회하는 과정에서 발생하는 무수히 많은 추가 SELECT 쿼리(N+1 문제)를 분석했습니다.
  * **해결책 1**: JPQL에서 **`JOIN FETCH`**를 사용하여 단 한 번의 쿼리로 댓글과 작성자 정보를 묶어 가져오도록 레포지토리를 최신화했습니다.
  * **해결책 2**: JPA의 **`@EntityGraph`**를 주입하여 연관된 엔티티를 Eager(즉시) 조회로 즉시 로딩하도록 최적화하는 방법을 학습했습니다.

### 4️⃣ 권한 인가 (ROLE_ADMIN) 및 소유권 검증
* **AdminController**: 특정 API 경로(`/admin/**`)는 오직 `ROLE_ADMIN` 권한을 가진 사용자만 접근이 가능하도록 SecurityConfig 필터 체인에 제한사항을 정의했습니다.
* **댓글 삭제 권한 검증**: 댓글 삭제 시 **"작성자 본인"**이거나 혹은 **"관리자(ADMIN) 권한 소지자"**인 경우에만 삭제가 가능하도록 서비스 단에서 소유권을 검증하는 로직을 견고하게 구현했습니다.

### 5️⃣ 카카오 OAuth2 소셜 로그인 & 트러블슈팅
* **OAuth2 Client**: `spring-boot-starter-security-oauth2-client` 의존성을 탑재하고 `application-kakao.yaml`에 카카오 전용 인가/토큰/사용자정보 엔드포인트 프로바이더를 등록했습니다.
* **CustomOAuth2UserService**: 카카오 서버로부터 로그인 성공 후 전달받은 유저 정보(JSON)를 Jackson 라이브러리를 통해 DTO(`KakaoOAuth2DTO`)로 매핑하고, 신규 회원일 경우 소셜 전용 계정으로 자동 가입 처리(Insert)하는 파이프라인을 구축했습니다.
* **CustomOAuth2User**: 일반 FormLogin으로 로그인한 세션 유저 객체(`CustomUserDetails`)와 OAuth2 소셜 로그인 유저 객체를 단일 상속 구조로 묶어 결합시킴으로써, 컨트롤러 레이어에서 로그인 타입과 무관하게 통일된 형태의 사용자 정보(`@AuthenticationPrincipal`)에 접근할 수 있도록 일원화했습니다.

---

## 🚨 트러블슈팅 (Troubleshooting)

### 📌 카카오 로그인 시 500 에러 (Password NOT NULL 제약조건 위반)
* **문제 상황**: 카카오 소셜 로그인을 성공적으로 진행한 후 최초 회원가입이 발생할 때, 데이터베이스에 사용자를 저장하지 못하고 아래와 같은 500 서버 오류가 발생했습니다.
  > `ERROR: null value in column "password" of relation "user_account" violates not-null constraint`
* **원인**: 소셜 사용자는 별도 비밀번호 입력 과정이 없으므로 엔티티 생성 시 패스워드를 빈 값(`null`)으로 삽입하려 했으나, 로컬 DB 테이블에 기존 일반 회원가입용 `password NOT NULL` 제약 조건이 해제되지 않고 유지되어 있어 트랜잭션이 롤백되는 현상이었습니다. (Hibernate `ddl-auto: update` 옵션은 기존의 NOT NULL 제약조건을 지워주지 못함)
* **해결**: 소셜 가입 회원 빌더에 임의의 랜덤 비밀번호(`UUID.randomUUID().toString()`)를 강제로 삽입해 줌으로써 DB 컬럼 제약조건을 우회하고 보안성을 확보하며 문제를 완벽히 해결했습니다.
  ```java
  // CustomOAuth2UserService.java
  UserAccountEntity oUser = UserAccountEntity.builder()
          .socialId(providerId)
          .socialProvider(registrationId)
          .username("%s_%s".formatted(providerId, registrationId))
          .password(java.util.UUID.randomUUID().toString()) // DB의 password NOT NULL 제약조건 만족
          .role("user")
          .build();
  ```
