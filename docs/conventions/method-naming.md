# 🔤 메서드 명명 규칙

## 0. 목차
- [1. 기본 규칙](#1-기본-규칙)
- [2. 계층별 네이밍 요약](#2-계층별-네이밍-요약)
- [3. Controller 메서드 네이밍](#3-controller-메서드-네이밍)
- [4. Service 메서드 네이밍](#4-service-메서드-네이밍)
- [5. Repository 메서드 네이밍](#5-repository-메서드-네이밍)
- [6. Boolean/컬렉션 네이밍](#6-boolean컬렉션-네이밍)
- [7. 인증/Auth 특수 동작 네이밍](#7-인증auth-특수-동작-네이밍)
- [8. 주의사항](#8-주의사항)

---

## 1. 기본 규칙
- 형식: `camelCase`
- 원칙: 동사로 시작 + 대상이 명확하게 보이게 작성
- 축약어 지양(예외: `id`, `url`, `jwt`, `api`, `dto` 등)

### 1.1 동작별 접두어 권장

| 목적 | 접두어 | 예시 |
|---|---|---|
| 조회(단건) | `get` | `getMission()`, `getMyProfile()` |
| 조회(목록) | `get + 복수` | `getMissions()`, `getFaqs()` |
| 생성 | `create` | `createInquiry()`, `createMission()` |
| 수정 | `update` | `updateOnboarding()`, `updateMyProfile()` |
| 삭제 | `delete` | `deleteWishlistMission()` |
| 검증 | `validate` | `validateRefreshToken()` |
| 처리(흐름) | `process` | `processMissionStatusChange()` |
| 전송 | `send` | `sendEmailVerificationCode()` |

---

## 2. 계층별 네이밍 요약

| 계층 | 네이밍 기준 | 예시 |
|---|---|---|
| Controller | REST 동작 + 대상 | `getTodayMissions()`, `refreshTodayMissions()` |
| Service | 도메인 유스케이스 중심 | `getRecommendedMissions()`, `changeMissionStatus()` |
| Repository | 영속화 책임(Spring Data 규칙) | `findByEmail()`, `existsByMemberIdAndDate()` |

---

## 3. Controller 메서드 네이밍
- `/members/me/**` 형태는 **My 접두어**를 권장합니다.
  - 예: `getMyProfile()`, `updateMyPassword()`, `getMyWalletHistory()`

### 3.1 API 명세 기반 예시

#### Common
- `GET /api/v1/health` → `healthCheck()`

#### Auth
- `POST /api/v1/auth/signup` → `signup()`
- `POST /api/v1/auth/login` → `login()`
- `POST /api/v1/auth/logout` → `logout()`
- `POST /api/v1/auth/refresh` → `reissueToken()`
- `POST /api/v1/auth/email-verifications` → `sendEmailVerificationCode()`
- `POST /api/v1/auth/email-verifications/verify` → `verifyEmailVerificationCode()`
- `POST /api/v1/auth/google` → `redirectGoogleLogin()` (리다이렉트라면)
- `POST /api/v1/auth/kakao` → `redirectKakaoLogin()`

#### Member
- `GET /api/v1/members/me` → `getMyProfile()`
- `PATCH /api/v1/members/me` → `updateMyProfile()`
- `PATCH /api/v1/members/me/password` → `updateMyPassword()`
- `DELETE /api/v1/members/me` → `withdraw()`
- `GET /api/v1/members/me/wallet` → `getMyWallet()`
- `GET /api/v1/members/me/wallet/history` → `getMyWalletHistory()`

#### Policy
- `GET /api/v1/policies/{policyId}` → `getPolicy()`
- `GET /api/v1/members/me/policies` → `getMyPolicyAgreements()`
- `POST /api/v1/members/me/policies` → `upsertMyPolicyAgreements()`

#### Preference / Interest
- `GET /api/v1/members/me/onboarding` → `getMyOnboarding()`
- `PATCH /api/v1/members/me/onboarding` → `updateMyOnboarding()`
- `GET /api/v1/interests` → `getInterests()`
- `GET /api/v1/interests/{interestId}/details` → `getInterestDetails()`

#### Mission
- `GET /api/v1/home/today-missions` → `getTodayMissions()`
- `POST /api/v1/home/today-missions/refresh` → `refreshTodayMissions()`
- `GET /api/v1/explore/categories/{categoryId}/missions` → `getMissionsByCategory()`
- `GET /api/v1/explore/missions/search` → `searchMissions()`
- `GET /api/v1/missions/{missionId}` → `getMission()`
- `POST /api/v1/missions/{missionId}/{status}` → `changeMissionStatus()` *(가능하면 PATCH 권장, 명세 유지 시 메서드명만 통일)*

#### Wishlist
- `POST /api/v1/members/me/wishlist/missions/{missionId}` → `addWishlistMission()`
- `DELETE /api/v1/members/me/wishlist/missions/{missionId}` → `deleteWishlistMission()`

#### MissionHistory (Space)
- `GET /api/v1/space/calendar` → `getMySpaceCalendar()`
- `GET /api/v1/space/summary` → `getMySpaceSummary()`
- `GET /api/v1/space/missions/completed` → `getMyCompletedMissions()`

#### Attendance
- `POST /api/v1/attendance/check` → `checkAttendance()`
- `GET /api/v1/attendance` → `getAttendanceMonthly()`
- `GET /api/v1/attendance/{date}` → `getAttendanceByDate()`

#### Item
- `GET /api/v1/home` → `getHome()`
- `GET /api/v1/items` → `getItems()`
- `GET /api/v1/items/{itemId}` → `getItem()`
- `POST /api/v1/members/me/purchases` → `purchaseItem()`
- `PATCH /api/v1/members/me/avatar/equipment` → `updateMyAvatarEquipment()`

#### Support
- `POST /api/v1/support/inquiries` → `createInquiry()`
- `GET /api/v1/members/me/support/inquiries` → `getMyInquiries()`
- `GET /api/v1/members/me/support/inquiries/{inquiryId}` → `getMyInquiry()`
- `PATCH /api/v1/admin/support/inquiries/{inquiryId}/answer` → `answerInquiry()` *(관리자)*
- `GET /api/v1/support/faqs` → `getFaqs()`
- `GET /api/v1/support/faqs/{faqId}` → `getFaq()`

---

## 4. Service 메서드 네이밍
- Controller 보다 “유스케이스”가 드러나게 작성
- 예시
  - `getRecommendedMissions(memberId, limit)`
  - `refreshRecommendedMissions(memberId, limit)`
  - `changeMissionStatus(memberId, missionId, status)`
  - `getMyWalletHistory(memberId, pageable)`
  - `upsertMyPolicyAgreements(memberId, request)`

---

## 5. Repository 메서드 네이밍
- Spring Data JPA 파생 메서드는 규칙 그대로 사용
  - 예: `findByEmail()`, `existsByMemberIdAndDate()`
- soft delete가 있으면 조건 포함 메서드명을 명시
  - 예: `findActiveById()`, `findByIdAndIsDeletedFalse()`

---

## 6. Boolean/컬렉션 네이밍
- Boolean prefix는 아래만 허용
  - `is`(상태): `isDeleted`, `isVerified`, `isAgreed`
  - `has`(소유): `hasNextPage`, `hasCompletedOnboarding`
  - `can`(가능): `canRefreshTodayMissions`
- 컬렉션은 복수형
  - `missions`, `interests`, `inquiries`, `walletTransactions`

---

## 7. 인증/Auth 특수 동작 네이밍
- 인증은 CRUD가 아니라 도메인 동작이므로 아래 이름을 예외적으로 “그대로” 허용
  - `signup`, `login`, `logout`, `withdraw`, `reissueToken`
- 이메일 인증은 동작을 명시
  - `sendEmailVerificationCode()`
  - `verifyEmailVerificationCode()`

---

## 8. 주의사항
- 규칙에 없는 네이밍은 팀 합의 후 반영
- Controller 메서드명은 “API 문서/명세”와 함께 봐도 바로 이해되도록 유지
