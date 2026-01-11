# 🦎  MOAMOA Backend






## 🚀 서비스 소개

---


## 👤 팀원 소개

---

## 🛠 기술 스택

### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- QueryDSL
- Spring Security
- Gradle (Groovy)
- Lombok

### Database
- MySQL
- Amazon RDS
- Redis

### Infrastructure / DevOps
- Amazon EC2
- Docker
- GitHub Actions (CI/CD)

### API & Documentation
- Swagger (OpenAPI)
---



## 📌 주요 기능



---

## 📂 프로젝트 구조

### 도메인 중심 패키지 구조
각 도메인 별로 관리하여 유지보수와 테스크에 용이합니다.
```
src/main/java/com/example/app
├── global                      # 전역 공통 모듈
│   ├── config                  # 설정 관련 클래스
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── WebConfig.java
│   ├── error                   # 전역 예외 처리
│   │   ├── ErrorCode.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── CustomException.java
│   ├── response                # 공통 응답 포맷
│   │   └── ApiResponse.java
│   └── util                    # 유틸리티 클래스
│       └── JwtUtil.java
│
├── member                      # 회원 도메인
│   ├── controller              # 회원 관련 API
│   │   └── MemberController.java
│   ├── service                 # 회원 비즈니스 로직
│   │   └── MemberService.java
│   ├── repository              # 회원 데이터 접근 계층
│   │   └── MemberRepository.java
│   ├── entity                  # 회원 엔티티
│   │   └── Member.java
│   ├── dto                     # 회원 DTO
│   │   ├── request
│   │   │   └── MemberSignupRequest.java
│   │   └── response
│   │       └── MemberResponse.java
│   └── exception               # 회원 도메인 예외
│       └── MemberException.java
│
└── Application.java             # Spring Boot 실행 클래스

```
---

## 🧾 개발 컨벤션
아래 문서를 기준으로 네이밍/협업 규칙을 통일합니다.

[![GitHub 협업 규칙](https://img.shields.io/badge/GitHub_협업_규칙-181717?style=for-the-badge&logo=github&logoColor=white)](docs/conventions/github-rule.md)

[![메서드 명명 규칙](https://img.shields.io/badge/메서드%20명명%20규칙-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](docs/conventions/method-naming.md)

[![클래스 명명 규칙](https://img.shields.io/badge/클래스%20명명%20규칙-E76F00?style=for-the-badge&logo=coffeescript&logoColor=white)](docs/conventions/class-naming.md)

[![필드 명명 규칙](https://img.shields.io/badge/필드%20명명%20규칙-0F766E?style=for-the-badge&logo=mysql&logoColor=white)](docs/conventions/field-naming.md)


---


## 🌱 브랜치 전략 & 커밋 컨벤션

브랜치/커밋/PR/리뷰 규칙은 아래 문서를 기준으로 운영합니다.
- 👉 [GitHub 협업 규칙 문서](docs/conventions/github-rule.md)

### Branch Roles (요약)
- `main`: 운영(배포) 브랜치 — 직접 커밋 금지, PR로만 병합
- `develop`: 개발 통합 브랜치 — 모든 작업 브랜치의 기본 병합 대상
- 작업 브랜치: `develop`에서 분기 → 작업 → `develop`으로 PR
- `hotfix/*`: 운영 긴급 수정 — `main`에서 분기 → `main` 병합 → `develop`에도 반영

### Branch Naming (요약)
형식: `type/work-desc/#issue-number`  
예시:
- `feat/login/#12`
- `fix/token-refresh/#34`
- `refactor/member-domain/#57`
- `chore/github-actions/#3`

### Commit Message (요약)
형식: `type: subject`  
예시:
- `feat: User 엔티티 및 레포지토리 생성`
- `fix: 토큰 재발급 로직 NPE 수정`






