# session-recovery

## 역할

이 훅은 에이전트 작업 중 오류가 발생하거나 예상치 못한 중단이 발생했을 때 세션을 복구합니다.

## 감지하는 오류 유형

1. **API 오류**: Rate limit, 타임아웃, 네트워크 오류
2. **코드 실행 오류**: 빌드 실패, 테스트 실패
3. **에이전트 루프**: 에이전트가 동일한 작업을 반복하는 경우
4. **컨텍스트 손실**: 이전 대화 컨텍스트를 잃어버린 경우

## 복구 전략

### API 오류 시
```
Rate Limit 오류 → 지수 백오프(Exponential Backoff)로 재시도
  - 1차 재시도: 5초 후
  - 2차 재시도: 15초 후
  - 3차 재시도: 60초 후
  - 3회 실패 → 사용자에게 알림
```

### 코드 실행 오류 시
```
빌드/테스트 실패 → Oracle에게 오류 분석 위임
  → 오류 원인 파악
  → Hephaestus에게 수정 지시
  → 최대 3회 자동 재시도
  → 여전히 실패 → 사용자에게 구체적인 오류 보고
```

### 에이전트 루프 감지
```
동일 작업 3회 반복 감지 → 루프 중단
  → 현재 상태 체크포인트 저장
  → Sisyphus에게 대안적 접근 방법 요청
```

## 설정

```jsonc
// oh-my-opencode.jsonc
{
  "hooks": {
    "session-recovery": {
      "enabled": true,
      "maxRetries": 3,
      "loopDetectionThreshold": 3,   // 3회 동일 작업 시 루프 감지
      
      // 체크포인트 저장 위치
      "checkpointDir": ".opencode/checkpoints",
      
      // 재시도 간격 (초)
      "retryIntervals": [5, 15, 60]
    }
  }
}
```

## 세션 복구 사용법

중단된 세션을 복구하려면:
```bash
# 마지막 체크포인트에서 재시작
opencode resume

# 특정 체크포인트에서 재시작
opencode resume --checkpoint .opencode/checkpoints/session-2024-01-15.json
```
