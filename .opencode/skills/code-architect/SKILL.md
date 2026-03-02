---
name: code-architect
description: >
  Use this skill for system design decisions, architecture planning, technology stack
  selection, or analyzing complex technical trade-offs. Triggered by phrases like
  "design the system", "choose the architecture", "which database", "API design",
  or "how should I structure this project?".
---

# Code Architect Skill

oh-my-opencode에서 시스템 설계 및 아키텍처 결정을 위한 스킬입니다.
이 스킬이 활성화되면 **Oracle** 에이전트가 주도권을 가져 심층 설계를 수행합니다.

## 에이전트 역할 분담

- **Oracle**: 아키텍처 리서치 및 최적 패턴 제안 (주도)
- **Librarian**: 관련 기술 문서 및 사례 연구 수집
- **Prometheus**: 구현 로드맵 및 마일스톤 계획 수립
- **Atlas**: 컴포넌트 의존성 및 데이터 흐름 다이어그램 작성

## 아키텍처 설계 프로세스

### 1단계: 요구사항 분석
```markdown
## 기능 요구사항
- [ ] 핵심 기능 목록화
- [ ] 사용자 스토리 정의
- [ ] API 계약 명세

## 비기능 요구사항
- [ ] 예상 동시 접속자 수
- [ ] 응답 시간 목표 (SLA)
- [ ] 가용성 요구사항 (99.9%?)
- [ ] 데이터 보존 정책
```

### 2단계: 기술 스택 선정 기준
| 항목 | 검토 포인트 |
|------|------------|
| **언어** | 팀 숙련도, 생태계, 성능 |
| **프레임워크** | 커뮤니티, 유지보수 활성도, 학습 곡선 |
| **데이터베이스** | 데이터 구조(관계형/비관계형), 쿼리 패턴, 스케일링 |
| **인프라** | 클라우드 vs 온프레미스, 비용, 운영 복잡성 |

### 3단계: 아키텍처 패턴 도구

```
# 모놀리식 vs 마이크로서비스 결정 트리
팀 규모 < 5명 → 모놀리식 (Modular Monolith)
서비스별 독립 배포 필요 → 마이크로서비스
이벤트 기반 처리 필요 → Event-Driven Architecture
```

## 산출물 형식

Oracle이 설계 완료 후 다음 문서를 생성합니다:
- `docs/architecture/README.md` - 전체 아키텍처 개요
- `docs/architecture/data-model.md` - 데이터 모델 및 ERD
- `docs/architecture/api-spec.md` - API 엔드포인트 명세
- `docs/architecture/adr/` - Architecture Decision Records (ADR)
