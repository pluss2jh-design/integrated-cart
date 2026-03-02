# todo-continuation-enforcer

## 역할

이 훅은 Sisyphus가 작업 중 멈추지 않고 TODO 항목을 끝까지 완료하도록 강제합니다.

## 동작 방식

에이전트 응답 사이클마다 실행되며, 다음을 확인합니다:

1. **TODO 항목 잔존 여부**: 현재 작업 컨텍스트에 미완료 TODO가 있는지 확인
2. **중단 감지**: 에이전트가 불필요하게 사용자 확인을 요청하는지 감지
3. **자동 계속**: 사용자 개입 없이 다음 TODO 항목으로 자동 진행

## 설정

```jsonc
// oh-my-opencode.jsonc 에서 이 훅 설정
{
  "hooks": {
    "todo-continuation-enforcer": {
      "enabled": true,
      // 사용자 확인 없이 자동으로 계속할 최대 TODO 수
      "maxAutoSteps": 10,
      // 각 단계 사이 대기 시간 (ms)
      "stepDelayMs": 500
    }
  }
}
```

## 예시 시나리오

```
Sisyphus: "다음 단계들을 구현하겠습니다:
- [x] 데이터베이스 스키마 설계
- [ ] API 엔드포인트 구현
- [ ] 프론트엔드 UI 작성
- [ ] 테스트 작성"

→ todo-continuation-enforcer 발동
→ Sisyphus가 사용자에게 묻지 않고 자동으로 다음 항목 진행
```

## 비활성화 방법

특정 작업에서 비활성화하려면:
```
"일단 멈춰" 또는 "확인 후 계속해줘" 라고 말하면 훅이 일시 중지됩니다.
```
