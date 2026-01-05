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

## 🌱 브랜치 전략

## Branch Strategy

본 프로젝트는 **Git Flow**를 기반으로 하되,  
`release` 브랜치는 사용하지 않고 **`main` / `develop` / 작업 브랜치** 구조로 운영합니다.


## 1) Branch Roles

### main
- 운영(Production) 배포 브랜치
- 실제 서비스에 배포되는 코드만 존재
- 직접 커밋 ❌ (Pull Request를 통해서만 병합)

### develop
- 다음 배포를 위한 개발 통합 브랜치
- 모든 기능 및 수정 브랜치의 기본 병합 대상
- 배포 전 최종 검증이 이루어지는 브랜치

### 작업 브랜치 (feature / fix / refactor 등)
- 신규 기능, 버그 수정, 리팩토링 등 개별 작업 단위 브랜치
- `develop`에서 분기하여 작업 후 `develop`으로 Pull Request 생성

### hotfix/
- 운영 환경에서 발생한 긴급 버그 수정용 브랜치
- `main`에서 분기하여 수정 후 `main`에 병합
- 병합 후 동일 내용을 `develop`에도 반영


## 2) Branch Naming Convention

작업 브랜치는 아래 규칙을 따릅니다.

### 형식 
`이슈타입/작업-설명/#이슈번호`
### 예시
- `feat/login/#12`
- `fix/token-refresh/#34`
- `refactor/member-domain/#57`
- `chore/github-actions/#3`


---

## 💬 커밋 컨벤션

`[타입] 작업 내용 `

| Type | 설명 |
|------|------|
| feat | 새로운 기능 |
| fix  | 버그 수정 |
| refactor | 리팩토링 |
| test | 테스트 코드 |
| docs | 문서 수정 |
| chore | 빌드/설정 |
| style | 코드 스타일 변경 |
| ci   | CI/CD 수정 |
| perf | 성능 개선 |
| remove | 불필요한 코드/파일 삭제 |


--- 





