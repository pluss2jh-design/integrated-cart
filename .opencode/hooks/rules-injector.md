# rules-injector

## 역할

이 훅은 작업 컨텍스트에 따라 적절한 규칙(`.sisyphus/rules/`)을 동적으로 Sisyphus의 프롬프트에 주입합니다.

## 동작 방식

작업 유형을 분석하여 관련된 규칙 파일을 자동으로 로드합니다.

### 규칙 주입 트리거

| 작업 유형 | 주입되는 규칙 |
|-----------|--------------|
| 코드 작성/수정 | `coding-standards.md` |
| Git 작업 | `git-workflow.md` (별도 파일 시 주입) |
| 보안 관련 코드 | `security.md` |
| 에이전트 위임 | `multi-agent-orchestration.md` |
| 데이터베이스 작업 | `database.md` (있을 경우) |
| API 설계 | `api-design.md` (있을 경우) |

### 규칙 파일 발견 순서

```
1. .sisyphus/rules/ 폴더 스캔
2. 파일명으로 작업 연관성 판단
3. YAML frontmatter의 'triggers' 필드 참조 (있을 경우)
4. 관련된 모든 규칙을 Sisyphus 컨텍스트에 병합
```

## 규칙 파일 형식

규칙 파일에 frontmatter를 추가하여 주입 조건을 명시할 수 있습니다:

```markdown
---
# 이 규칙이 주입되는 조건 (선택적)
triggers:
  - "auth"
  - "login"
  - "jwt"
  - "session"
# 우선순위 (높을수록 먼저 적용)
priority: 10
# 항상 주입 여부
alwaysOn: false
---

# 인증 관련 규칙

...규칙 내용...
```

## 설정

```jsonc
// oh-my-opencode.jsonc
{
  "hooks": {
    "rules-injector": {
      "enabled": true,
      
      // 규칙 파일 위치 (기본값)
      "rulesDir": ".sisyphus/rules",
      
      // 최대 주입 규칙 수 (컨텍스트 길이 제한)
      "maxRules": 5,
      
      // alwaysOn 규칙 목록 (이름 기준)
      "alwaysOn": [
        "coding-standards",
        "security"
      ]
    }
  }
}
```
