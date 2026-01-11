# 🗄️ 필드명 규칙 컨벤션 (Entity/DTO/DB/JSON)

## 0. 범위
이 문서는 아래 네이밍을 통일합니다.
- Java(Entity/DTO) 필드명
- DB 컬럼명 (MySQL/RDS 기준 권장)
- API(JSON) 필드명
- 상수/Enum

---

## 1. Java(Entity/DTO) 필드 네이밍

### 1.1 기본 규칙
- 형식: **lowerCamelCase**
- 의미가 분명한 단어 선택 (동의어 혼용 금지)
  - 예: `member`로 통일 (user/member 혼용 금지)
- 축약어 지양(예외: `id`, `url`, `jwt`, `api`)

### 1.2 ID 필드 규칙
- Entity PK는 `id` 단일 사용
- 연관관계 FK는 **객체 참조** 권장
  - 권장: `private Member member;`
  - 지양: `private Long memberId;` *(DTO에서는 허용)*

DTO에서만 필요한 경우:
- `memberId`, `missionId`, `policyId`, `itemId`, `inquiryId`

### 1.3 시간/감사(Audit) 필드
- 생성/수정: `createdAt`, `modifiedAt` **(한 가지로 통일)**
- 소프트 삭제(선택): `isDeleted`, `deletedAt`
- 만료/유효기간: `expiresAt` (예: 이메일 인증코드, 리프레시 토큰)

### 1.4 Boolean 필드
prefix는 아래만 허용:
- 상태: `isVerified`, `isDeleted`, `isActive`
- 소유: `hasCompletedOnboarding`, `hasNextPage`
- 가능: `canRefresh`, `canEdit`

### 1.5 컬렉션 필드
- 항상 복수형
  - `missions`, `interests`, `inquiries`, `walletTransactions`, `policyAgreements`

---

## 2. DB 컬럼 네이밍 (MySQL/RDS)

### 2.1 기본 규칙
- 형식: **snake_case**
- Java 필드와 의미 동일하게 유지
  - Java: `createdAt` → DB: `created_at`
  - Java: `isDeleted` → DB: `is_deleted`

### 2.2 컬럼 표준 예시
- `id` (PK)
- `created_at`, `modified_at`
- `is_deleted`, `deleted_at`

### 2.3 MOAMOA 도메인 예시
- Member
  - `email`, `password`, `nickname`, `profile_image_url`
- Mission
  - `title`, `description`, `reward_amount`, `mission_status`
- Attendance
  - `attendance_date`, `is_attended`
- Wallet
  - `balance`
- Inquiry
  - `title`, `content`, `answer_content`, `answered_at`

### 2.4 JPA 매핑 권장
- DB가 snake_case면 엔티티에 `@Column(name = "created_at")` 명시 권장
- 인덱스는 “조회 조건/정렬/조인” 기준으로 설계

---

## 3. API(JSON) 필드 네이밍
- 기본: **lowerCamelCase** 권장 (프론트와 합의해 통일)
- 응답 공통 래퍼: `status`, `message`, `data`

### 3.1 예시 
```json
{
  "status": 200,
  "message": "OK",
  "data": {
    "memberId": 1,
    "nickname": "moamoa",
    "createdAt": "2026-01-01T12:00:00Z"
  }
}

```
---

## 4. 상수/Enum 네이밍

### 4.1 상수(Constants)
- `public static final` 상수는 **UPPER_SNAKE_CASE**
- 의미가 모호한 이름 금지 (`VALUE`, `TEMP` 같은 거)
- 단위가 있으면 단위를 포함 (`*_SECONDS`, `*_DAYS`)

예시:
- `DEFAULT_PAGE_SIZE`
- `MAX_UPLOAD_SIZE_MB`
- `REFRESH_TOKEN_EXPIRES_DAYS`
- `EMAIL_CODE_EXPIRES_SECONDS`

### 4.2 Enum
- Enum 클래스명: **PascalCase**
- Enum 값: **UPPER_SNAKE_CASE**
- 상태/구분값은 의미가 드러나는 접미사 사용: `Status`, `Type`, `Role`, `Category`

예시:
- `MemberRole` → `USER`, `ADMIN`
- `MissionStatus` → `READY`, `IN_PROGRESS`, `COMPLETED`
- `InquiryStatus` → `PENDING`, `ANSWERED`

---

## 5. 네이밍 충돌 방지 규칙 
- `member` vs `user` 혼용 금지 → **member로 통일**
- `modifiedAt` vs `updatedAt` 혼용 금지 → **modifiedAt로 통일**(권장)
- “ID 전달”은 DTO에서만 허용: `memberId`, `missionId` …
- “응답 래퍼” 필드명은 항상 고정: `status`, `message`, `data`

---

## 6. 실무 체크리스트
PR 올리기 전 아래를 확인합니다.
- [ ] Entity/DTO 필드명이 lowerCamelCase 인가?
- [ ] DB 컬럼명이 snake_case 인가?
- [ ] createdAt / modifiedAt 네이밍이 프로젝트 전체에서 통일됐는가?
- [ ] Boolean prefix가 is/has/can 규칙을 지켰는가?
- [ ] API JSON 필드명이 lowerCamelCase 로 통일됐는가?
- [ ] 같은 의미의 용어를 혼용하지 않았는가? (member/user 등)
