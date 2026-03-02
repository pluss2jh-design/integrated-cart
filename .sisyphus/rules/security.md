# 보안 정책 규칙

이 규칙은 모든 에이전트가 코드 작성 또는 검토 시 반드시 준수해야 하는 보안 정책입니다.

## 필수 보안 원칙

### 비밀 정보 관리
- API 키, 비밀번호, JWT Secret 등 **절대 코드에 하드코딩 금지**
- 모든 비밀값은 환경 변수(`process.env.VARIABLE_NAME`)로 접근
- `.env` 파일은 `.gitignore`에 반드시 포함
- 저장소에는 `.env.example`만 커밋 (실제 값 없이 키 이름만)

### 입력값 검증 (Input Validation)
```typescript
// ✅ Zod를 활용한 서버 사이드 검증 예시
import { z } from 'zod';

const CreateUserSchema = z.object({
  email: z.string().email('유효한 이메일 형식이 아닙니다'),
  password: z.string()
    .min(8, '비밀번호는 8자 이상이어야 합니다')
    .regex(/[A-Z]/, '대문자를 포함해야 합니다')
    .regex(/[0-9]/, '숫자를 포함해야 합니다'),
  name: z.string().min(1).max(50).trim(),
});
```

### SQL Injection 방지
```typescript
// ❌ 위험한 패턴
const query = `SELECT * FROM users WHERE email = '${email}'`;

// ✅ 파라미터화된 쿼리 사용
const user = await db.query(
  'SELECT * FROM users WHERE email = $1',
  [email]
);

// ✅ ORM (Prisma) 사용
const user = await prisma.user.findUnique({ where: { email } });
```

### 인증/인가
- JWT는 **`httpOnly` + `secure` + `sameSite: strict` 쿠키**에 저장
- `localStorage`에 JWT 저장 금지 (XSS 취약점)
- API 엔드포인트마다 인증 미들웨어 적용 필수
- 권한 검사는 서버 사이드에서만 수행

## Hephaestus에 대한 지시사항

코드 작성 시 다음 항목을 자동으로 확인한다:
1. 환경 변수 접근 패턴 적용 여부
2. 사용자 입력 검증 코드 포함 여부  
3. SQL 파라미터화 또는 ORM 사용 여부
4. 인증/인가 미들웨어 적용 여부

## 민감 정보 노출 금지

에이전트는 절대 다음 정보를 응답에 포함하지 않는다:
- 실제 API 키, 비밀번호, 토큰
- 개인식별정보(이름, 이메일, 전화번호, 주민번호)
- 금융 정보(카드번호, 계좌번호)
- 내부 시스템 경로나 서버 IP 주소
