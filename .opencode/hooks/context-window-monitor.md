# context-window-monitor

## 역할

이 훅은 AI 모델의 컨텍스트 윈도우 사용량을 모니터링하고, 한계에 근접하면 자동으로 컨텍스트를 압축하거나 요약합니다.

## 동작 조건

컨텍스트 윈도우 사용량이 **80%** 이상일 때 자동으로 발동됩니다.

## 동작 방식

### 단계별 대응

```
사용량 60% → 경고: 불필요한 컨텍스트 정리 권고
사용량 80% → 자동 압축: 오래된 대화 내용을 요약으로 대체
사용량 95% → 긴급 압축: 핵심 정보만 남기고 나머지 삭제
```

### 압축 우선순위

보존 (높은 우선순위):
1. 현재 작업의 명세 요구사항
2. 최근 에러 메시지 및 디버깅 정보
3. 현재 작업 중인 코드 파일 내용
4. 미완료 TODO 목록

삭제 가능 (낮은 우선순위):
1. 완료된 단계의 상세 대화 내용
2. 중간 계획 단계 (최종 결과만 보존)
3. 반복된 지시사항

## 설정

```jsonc
// oh-my-opencode.jsonc
{
  "hooks": {
    "context-window-monitor": {
      "enabled": true,
      "warningThreshold": 0.60,   // 60%에서 경고
      "autoCompressThreshold": 0.80, // 80%에서 자동 압축
      "emergencyThreshold": 0.95,    // 95%에서 긴급 처리
      
      // 압축 방법: "summarize" | "truncate" | "checkpoint"
      "compressionMethod": "summarize"
    }
  }
}
```

## 체크포인트 저장

긴급 압축 전 현재 상태를 `.opencode/checkpoints/` 에 저장합니다:
```
.opencode/checkpoints/
  └── session-2024-01-15-14-30.json  ← 세션 체크포인트
```
