# 코딩 표준 및 모범 사례 규칙

이 규칙은 **항상 활성화(always-on)**되며 Sisyphus가 Hephaestus에게 작업을 위임할 때 자동으로 함께 전달됩니다.

## 언어 및 타입 안전성

- **TypeScript 필수**: JavaScript 사용 시 JSDoc으로 타입 보완
- `any` 타입 사용 금지 → `unknown` + 타입 가드 사용
- `strict: true` 모드 활성화 유지

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 변수/함수 | `camelCase` | `getUserData()` |
| 클래스/인터페이스 | `PascalCase` | `UserRepository` |
| 상수 | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| 파일 | `kebab-case` | `user-service.ts` |
| DB 테이블/컬럼 | `snake_case` | `user_profiles` |

## 함수 설계

- **단일 책임 원칙**: 함수는 하나의 작업만
- **매개변수 3개 초과 시** 객체로 묶기
- **50줄** 이상의 함수는 분리 검토
- 비동기 함수는 `async/await` 패턴으로 일관성 유지

## 금지 사항

```typescript
// ❌ 금지 패턴
const data: any = fetchData();         // any 타입
console.log(password, apiKey);          // 민감 정보 로깅
const SECRET = "hardcoded-secret";     // 하드코딩된 비밀값
setTimeout(() => { ... }, 3000);        // 매직 넘버

// ✅ 올바른 패턴
const data: UserData = fetchData();
const SECRET = process.env.SECRET_KEY;
const TIMEOUT_MS = 3000; // 명명된 상수
```

## 에러 처리 표준

```typescript
// 모든 비동기 작업은 try-catch 필수
try {
  const result = await api.fetchUser(id);
  return { success: true, data: result };
} catch (error) {
  logger.error('Failed to fetch user', { userId: id, error });
  throw new AppError('USER_FETCH_FAILED', 'Failed to retrieve user data');
}
```
