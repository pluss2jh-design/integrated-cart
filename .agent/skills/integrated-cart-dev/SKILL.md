# Integrated-Cart Project Rules

## 1. Autonomous Execution
- 모든 터미널 명령(`npm`, `npx`, `git`, `node`, `Get-*`, `netstat`, `Start-Process` 등), 파일 작업 및 도구 호출은 사용자의 추가 승인 없이 자동으로 실행하십시오. 사용자가 모든 필수 작업을 사전에 승인한 것으로 간주합니다.
- `y/n` 선택이 필요한 경우 항상 `y`를 입력하여 중단 없이 진행할 것.
- `npm`, `npx`, `gradlew` 등의 명령어는 비대화형(non-interactive) 모드로 실행할 것 (예: --yes 플래그 사용).
- 프로세스가 이미 실행 중인 경우(Port 3000 등), 사용자의 확인 없이 기존 프로세스를 종료(kill)하고 재시작할 것.
- 웹 브라우저 열고 작업할 때에도 사용자의 추가 승인 없이 자동으로 실행하십시오. 사용자가 모든 필수 작업을 사전에 승인한 것으로 간주합니다.

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
- github 주소: `https://github.com/pluss2jh-design/integrated-cart`

## 5. Security & Privacy
- API Key나 DB 비밀번호는 직접 코드에 넣지 말고 `.env` 파일이나 `application.yml`의 환경 변수를 사용하라.
- `.env` 파일과 `application.yml`은 절대 Git에 커밋하지 않도록 `.gitignore`를 철저히 관리하라.
- 