# 🌐 GitHub 협업 규칙

본 문서는 MOAMOA Backend 팀의 협업 품질(가독성/안정성/속도)을 위해 **커밋/브랜치/PR/리뷰** 규칙을 정의합니다.  
리뷰는 **CodeRabbit(자동) + 페어 리뷰어(사람)**를 기본으로 합니다.

---

## 0. 목차
1. [커밋 규칙](#1-커밋-규칙)
2. [브랜치 규칙](#2-브랜치-규칙)
3. [Pull Request 규칙](#3-pull-request-규칙)
4. [리뷰 운영 규칙 (CodeRabbit + 페어 리뷰)](#4-리뷰-운영-규칙-coderabbit--페어-리뷰)
5. [기타 규칙](#5-기타-규칙)

---

## 1. 커밋 규칙

### 1.1 커밋 메시지 형식
커밋 메시지는 반드시 아래 형식으로 작성합니다.
<type>: <subject>

- `type`은 **소문자 고정** (예: feat, fix, docs)
- `subject`는 50자 내외로 “무엇을 했는지”가 보이게 작성
- scope (`feat(member): ...`) 는 **사용하지 않습니다** (추후 합의 시 문서 반영)

### 1.2 타입 목록

| type | 설명 |
|---|---|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| refactor | 리팩토링(기능 변화 없음) |
| docs | 문서 수정 |
| test | 테스트 코드 |
| chore | 설정/정리 |
| hotfix | 운영 긴급 수정 |
| ci | CI/CD |
| perf | 성능 개선 |
| style | 포맷/정렬(기능 변화 없음) |
| move | 파일/폴더 이동 |
| rename | 이름 변경 |
| del | 삭제 |

### 1.3 예시
- `feat: User 엔티티 및 레포지토리 생성`
- `fix: 토큰 재발급 로직 NPE 수정`
- `docs: GitHub 협업 규칙 문서 업데이트`

---

## 2. 브랜치 규칙

### 2.1 브랜치 역할
- `main`
  - 운영(Production) 배포 브랜치
  - 직접 커밋 금지 ❌ (PR로만 병합)
- `develop`
  - 개발 통합 브랜치
  - 모든 작업 브랜치의 기본 병합 대상
- 작업 브랜치
  - `develop`에서 분기 → 작업 → `develop`로 PR
- `hotfix/*`
  - 운영 긴급 수정 브랜치
  - `main`에서 분기 → `main`에 병합
  - 병합 후 **동일 변경을 develop에도 반영(PR 또는 merge)**

### 2.2 브랜치 네이밍
**형식**
<type>/<work-desc>/#<issue-number>

- `<type>`: `feat | fix | refactor | docs | chore | hotfix`
- `<work-desc>`: 영문 소문자 + kebab-case 권장 (`token-refresh`, `member-signup`)
- 이슈가 없으면 `#0` 허용(팀 합의)

**예시**
- `feat/login/#12`
- `fix/token-refresh/#34`
- `refactor/member-domain/#57`
- `chore/github-actions/#3`

> `#` 포함 브랜치는 터미널에서 따옴표로 다루는 것을 권장합니다.  
> 예) `git switch "docs/conventions/#19"`

---
## 3. Pull Request 규칙

### 3.1 PR 생성 기본
- PR 대상 브랜치: **develop**
- PR 제목 형식(팀 합의): **`[Type] 작업 요약`**
  - Type 예: `Feat | Fix | Refactor | Docs | Chore | Hotfix | Test`
  - 예) `[Feat] 유저 도메인 엔티티 및 레포지토리 생성`
  - 예) `[Docs] GitHub 협업 규칙 문서 업데이트`
- PR 본문에는 반드시 아래를 포함
  - 변경 요약(무엇)
  - 변경 이유(왜)
  - 테스트/검증 방법(어떻게)

### 3.2 Issue 제목 규칙 (팀 합의)
- Issue 제목 형식: **`[Type] 작업 요약`**
  - 예) `[Feat] 로그인 기능 구현`
  - 예) `[Fix] 결제 승인 오류 수정`
  - 예) `[Docs] 컨벤션 문서 정리`

### 3.3 이슈 자동 종료
- PR 본문에 아래 키워드를 사용합니다.
  - `Closes #이슈번호`

---
## 4. 리뷰 운영 규칙 (CodeRabbit + 페어 리뷰)

### 4.1 역할 정의 (팀 합의)
- **Reviewee**: PR 작성자(작업자). PR 품질 1차 책임자.
- **Reviewer**: 지정 리뷰어. 리뷰 완료 및 승인 1차 책임자.
- 모든 PR은 **CodeRabbit(자동) + Reviewer(사람)** 리뷰를 기본으로 합니다.

### 4.2 리뷰 흐름
1) Reviewee가 PR 생성 → Reviewer 지정(Request review)
2) CodeRabbit 코멘트 확인/반영(또는 근거 남김)
3) Reviewer가 변경사항 검토 후 Approve 또는 Request changes
4) 모든 체크가 끝나면 develop에 merge

### 4.3 리뷰 페어링 (고정)
| 팀 | Reviewer | Reviewee |
|---|---|---|
| 1팀 | 배민 | 박콩 |
| 2팀 | 박콩 | 미카엘 |
| 3팀 | 제로 | 배민 |
| 4팀 | 준 | 제로 |
| 5팀 | 미카엘 | 준 |

> 예외 규칙: Reviewer 부재/지연 시(예: 장시간 미응답) 팀 내 합의로 다른 Reviewer로 교체 가능.

---

## 5. 기타 규칙
- `main` 브랜치는 항상 배포 가능한 상태 유지
- 불필요한 파일/코드는 머지 전 정리
- 규칙에 없는 상황은 팀 합의 후 문서에 반영
