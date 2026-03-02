# comment-checker

## 역할

이 훅은 Hephaestus가 코드를 작성하거나 수정한 후 자동으로 실행되어, 주석의 품질과 언어 일관성을 검사합니다.

## 검사 항목

### 1. 언어 일관성 검사
- 프로젝트의 주석 언어(한국어/영어)가 일관되게 유지되는지 확인
- 혼재 시 Sisyphus 규칙에 설정된 기본 언어로 통일 권고

### 2. 주석 필요성 검사
```typescript
// ❌ 불필요한 주석 (코드가 명확한 경우)
// 이름을 반환한다
function getName(): string {
  return this.name;
}

// ✅ 필요한 주석 (WHY를 설명, 비명확한 로직)
// OAuth 토큰 갱신 시 race condition 방지를 위해
// mutex 패턴으로 단일 요청만 허용한다
async function refreshToken() { ... }
```

### 3. TODO/FIXME 태그 검사
```typescript
// ✅ 올바른 TODO 형식
// TODO(홍길동): 캐싱 레이어 추가 필요 - #123 이슈 참조

// ❌ 불완전한 TODO (담당자, 이슈 번호 없음)
// TODO: 나중에 수정
```

### 4. JSDoc/TSDoc 형식 검사 (공개 API)
```typescript
/**
 * 사용자 프로필을 조회합니다.
 * 
 * @param userId - 조회할 사용자의 고유 ID
 * @returns 사용자 프로필 객체, 없으면 null
 * @throws {NotFoundError} 사용자가 존재하지 않을 경우
 */
async function getUserProfile(userId: string): Promise<UserProfile | null>
```

## 설정

```jsonc
// oh-my-opencode.jsonc
{
  "hooks": {
    "comment-checker": {
      "enabled": true,
      
      // 주석 언어: "ko" | "en" | "any"
      "language": "ko",
      
      // 공개 API에 JSDoc 필수 여부
      "requireJSDoc": true,
      
      // 최소 주석 비율 (0~1, 0이면 비율 검사 안 함)
      "minCommentRatio": 0,
      
      // 자동 수정 여부 (false면 경고만)
      "autoFix": false
    }
  }
}
```

## 비활성화

특정 파일에서 주석 검사를 제외하려면 파일 상단에 추가:
```typescript
// omo-comment-checker: disable
```
