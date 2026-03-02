---
name: git-master
description: >
  Use this skill to manage Git operations for the repository
  (https://github.com/pluss2jh-design/integrated-cart).
  Triggered by phrases like "commit this", "create a branch", "merge", "git commit", or "push".
---

# Git Master Skill

이 스킬은 리포지토리(https://github.com/pluss2jh-design/integrated-cart)의 Git 버전 관리를 전문적으로 수행합니다.

## 에이전트 역할 분담

이 스킬이 실행될 때 omo는 다음과 같이 에이전트를 조율합니다:
- **Prometheus**: 커밋 전략을 계획하고 변경사항을 논리적으로 분류
- **Hephaestus**: 실제 git 명령어를 실행
- **Momus**: 커밋 메시지 품질을 검토

## 커밋 메시지 형식 (Conventional Commits)

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

### Type 분류
| Type | 사용 상황 |
|------|-----------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경만 |
| `style` | 코드 포맷팅 (로직 변경 없음) |
| `refactor` | 리팩토링 (기능 변경 없음) |
| `perf` | 성능 개선 |
| `test` | 테스트 추가/수정 |
| `chore` | 빌드, 의존성 등 기타 변경 |
| `ci` | CI/CD 파이프라인 변경 |

## 브랜치 전략

```
main          ← 프로덕션 (직접 커밋 금지)
develop       ← 개발 통합
feature/*     ← 기능 개발
hotfix/*      ← 긴급 수정
release/*     ← 릴리즈 준비
```

## 원자적 커밋 절차

```bash
# 1. 변경 파일 확인
git status

# 2. 논리적 단위로 스테이징
git add <specific-files>

# 3. 스테이징 확인
git diff --staged

# 4. 커밋
git commit -m "feat(api): add user authentication endpoint"
```

## 자동화 스크립트

더 복잡한 Git 작업은 `scripts/` 폴더의 스크립트를 활용합니다.
