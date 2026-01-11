# 🧱 클래스 명명 규칙

## 0. 목차
- [1. 기본 명명 규칙](#1-기본-명명-규칙)
- [2. 도메인/계층별 클래스 네이밍](#2-도메인계층별-클래스-네이밍)
- [3. Entity 클래스 네이밍](#3-entity-클래스-네이밍)
- [4. 예외/에러코드 네이밍](#4-예외에러코드-네이밍)
- [5. Enum 네이밍](#5-enum-네이밍)
- [6. 외부 연동/유틸/설정 클래스 네이밍](#6-외부-연동유틸설정-클래스-네이밍)
- [7. 규칙 제외(프레임워크 역할 기반)](#7-규칙-제외프레임워크-역할-기반)

---

## 1. 기본 명명 규칙
- **형식**: `PascalCase`
- **원칙**
  - 역할/책임이 이름만으로 드러나도록 명사형 사용
  - 약어 지양(예외: `Id`, `Url`, `Jwt`, `Api`, `Dto` 등 팀 합의된 약어)
  - “무슨 도메인인지”가 모호하면 도메인 접두어를 붙임

---

## 2. 도메인/계층별 클래스 네이밍

### 2.1 도메인 분류 (API 명세 기준 예시)
- `Auth`, `Member`, `Policy`, `Preference(Interest)`, `Mission`, `Wishlist`, `MissionHistory(Space)`, `Attendance`, `Item`, `Support`, `Admin`

### 2.2 역할별 접미사 규칙
| 역할 | 접미사 | 예시 |
|---|---|---|
| Controller | `Controller` | `AuthController`, `MemberController`, `MissionController`, `AttendanceController` |
| Service | `Service` | `AuthService`, `MemberService`, `MissionService`, `SupportService` |
| Repository | `Repository` | `MemberRepository`, `MissionRepository`, `InquiryRepository`, `FaqRepository` |
| Config | `Config` | `SecurityConfig`, `SwaggerConfig`, `RedisConfig`, `JwtConfig` |
| DTO | `RequestDto`, `ResponseDto` | `SignupRequestDto`, `MyProfileResponseDto`, `TodayMissionResponseDto` |
| 구현체(선택) | `Impl` | `MissionServiceImpl` (인터페이스를 둘 때만) |

---

## 3. Entity 클래스 네이밍
- **단수형 명사 + PascalCase**
- `Entity` 접미사 금지 (중복/가독성 저하)
- 연관관계/매핑 엔티티는 **의미가 명확한 복합 명사** 사용

### 3.1 예시 
- 회원/인증
  - `Member`, `EmailVerification`, `RefreshToken`
- 약관/동의
  - `Policy`, `MemberPolicyAgreement` (또는 `PolicyAgreement`)
- 온보딩/관심사
  - `Interest`, `InterestDetail`, `MemberOnboarding`
- 미션/찜/히스토리
  - `Mission`, `MissionCategory`(또는 `Category`), `Wishlist`, `MissionHistory`
- 출석
  - `Attendance`
- 아이템/상점/구매/착장
  - `Item`, `Purchase`, `AvatarEquipment`
- 도토리/지갑(명세에 wallet 존재)
  - `Wallet`, `WalletTransaction`
- 고객지원
  - `Inquiry`, `Faq`

### 3.2 추상/공통 엔티티(허용)
- `BaseEntity`, `BaseTimeEntity` 등 공통 감사 필드용 추상 엔티티는 허용

---

## 4. 예외/에러코드 네이밍
- 도메인별 에러코드 Enum: `XxxErrorCode`
  - 예: `AuthErrorCode`, `MemberErrorCode`, `MissionErrorCode`, `SupportErrorCode`
- 도메인별 커스텀 예외: `XxxException`
  - 예: `AuthException`, `MemberException`, `MissionException`, `AttendanceException`

---

## 5. Enum 네이밍
- Enum 클래스: `PascalCase` + 의미 접미사 (`Status`, `Type`, `Role`, `Category` 등)
- Enum 값: `UPPER_SNAKE_CASE`

### 5.1 예시 
- `MissionStatus` : `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED` (실제 상태는 팀 합의대로)
- `WalletTransactionType` : `EARN`, `SPEND`
- `MemberRole` : `USER`, `ADMIN`

---

## 6. 외부 연동/유틸/설정 클래스 네이밍

### 6.1 Util (순수 함수성)
- `JwtUtil`, `DateTimeUtil`, `MaskingUtil`

### 6.2 Provider (생성 책임)
- `JwtTokenProvider`, `EmailVerificationCodeProvider`

### 6.3 Client (외부 API 통신)
- `KakaoOAuthClient`, `GoogleOAuthClient`, `EmailSenderClient`

### 6.4 Handler (흐름 제어/예외 처리)
- `GlobalExceptionHandler`, `AuthFailureHandler`

---

## 7. 규칙 제외(프레임워크 역할 기반)
프레임워크/라이브러리 구조상 역할이 명확한 경우 예외적으로 접미사를 그대로 사용합니다.

| 접미사 | 예시 |
|---|---|
| `Interceptor` | `AuthInterceptor` |
| `Filter` | `JwtAuthenticationFilter` |
| `EntryPoint` | `JwtAuthenticationEntryPoint` |
| `Scheduler` | `TodayMissionRefreshScheduler` (사용 시) |
