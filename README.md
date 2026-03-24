# 🏗️ 시스템 아키텍처 100일 스터디

> 설계 원칙부터 분산 시스템까지, 하루 하나씩 쌓아가는 백엔드 아키텍처 학습 기록

## 소개

백엔드 / 클라우드 / 분산 시스템 아키텍처를 100일 동안 학습하는 프로젝트입니다.  
매일 하나의 토픽을 공부하고, 코드 실습과 TIL을 기록합니다.

- **기간:** 100일 (하루 1토픽)
- **언어:** Java
- **블로그:** [티스토리](https://yourname.tistory.com) *(링크 수정 필요)*

## 커리큘럼

| Phase | 기간 | 테마 |
|-------|------|------|
| 1 | Day 1–25 | 기초 다지기 — 설계 원칙 & 네트워크 |
| 2 | Day 26–50 | 백엔드 아키텍처 심화 |
| 3 | Day 51–75 | 클라우드 & 인프라 |
| 4 | Day 76–100 | 분산 시스템 & 통합 설계 |

## 진행 현황

### Phase 1 — 기초 다지기
- [x] Day 1 — SOLID 원칙
- [x] Day 2 — SRP 심화 & 관심사 분리
- [x] Day 3 — 결합도와 응집도
- [ ] Day 4 — DRY, KISS, YAGNI
- [ ] Day 5 — 설계 원칙 종합 정리
- [ ] Day 6–10 — HTTP/TCP, REST vs gRPC
- [ ] Day 11–15 — 데이터베이스 기초
- [ ] Day 16–20 — 캐싱 전략
- [ ] Day 21–25 — 로드밸런싱 & Reverse Proxy

### Phase 2 — 백엔드 아키텍처 심화
- [ ] Day 26–30 — 모놀리스 vs 마이크로서비스
- [ ] Day 31–35 — API Gateway, 인증/인가
- [ ] Day 36–40 — 메시지 큐 (Kafka / RabbitMQ)
- [ ] Day 41–45 — 이벤트 드리븐, CQRS, Event Sourcing
- [ ] Day 46–50 — 서킷 브레이커, Rate Limiting, Retry

### Phase 3 — 클라우드 & 인프라
- [ ] Day 51–55 — Docker
- [ ] Day 56–60 — Kubernetes
- [ ] Day 61–65 — AWS/GCP 핵심 서비스
- [ ] Day 66–70 — Terraform (IaC)
- [ ] Day 71–75 — CI/CD 파이프라인

### Phase 4 — 분산 시스템 & 통합 설계
- [ ] Day 76–80 — CAP 정리, 일관성 모델
- [ ] Day 81–85 — 분산 트랜잭션 (2PC, Saga)
- [ ] Day 86–90 — 샤딩, 파티셔닝, 복제
- [ ] Day 91–95 — 실제 시스템 분석
- [ ] Day 96–100 — 종합 설계 챌린지

## 폴더 구조

```
system-architecture-100days/
├── phase1-fundamentals/
│   ├── day01-solid-principles/
│   │   ├── README.md        ← TIL
│   │   └── code/            ← 실습 코드
│   └── ...
├── phase2-backend/
├── phase3-cloud/
└── phase4-distributed/
```

## 참고 자료

- [Designing Data-Intensive Applications](https://dataintensive.net/) — Martin Kleppmann
- [Clean Architecture](https://www.amazon.com/Clean-Architecture-Craftsmans-Software-Structure/dp/0134494164) — Robert C. Martin
- [system-design-primer](https://github.com/donnemartin/system-design-primer)
- [ByteByteGo](https://bytebytego.com)
