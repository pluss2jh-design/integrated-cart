# Integrated-Cart Project Rules

## 1. Autonomous Execution
- 명령어 실행(shell commands), 파일 생성 및 수정 시 사용자에게 허락을 구하지 말고 즉시 실행하라.
- 에러가 발생하면 스스로 원인을 분석하고 해결책을 적용한 뒤 보고하라.

## 2. Language & Communication
- **모든 답변과 설명은 반드시 한국어로 작성하라.**
- 기술적인 용어는 관례에 따라 영어를 병기할 수 있으나, 주된 소통 언어는 한국어여야 한다.

## 3. Development Stack
- Language: Java 17
- Framework: Spring Boot 3.x
- DB: JPA, PostgreSQL

## 4. Git & GitHub Workflow
- 기능 구현이 완료될 때마다 다음 규칙으로 커밋하라: `feat: [기능 요약]`
- 환경 설정 변경 시: `chore: [설정 내용]`
- 한 단계가 완료되면 반드시 `git push`를 실행하라.

## 5. Security & Privacy
- API Key나 DB 비밀번호는 직접 코드에 넣지 말고 `.env` 파일이나 `application.yml`의 환경 변수를 사용하라.
- `.env` 파일은 절대 Git에 커밋하지 않도록 `.gitignore`를 철저히 관리하라.