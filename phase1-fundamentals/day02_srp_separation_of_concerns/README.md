## Day 02 — SRP 심화 & 관심사 분리 (Separation of Concerns)

### 오늘 배운 것

SRP의 진짜 정의는 "하나의 일만 해야 한다"가 아니다.
Robert C. Martin은 *Clean Architecture*(2017)에서 SRP를 이렇게 재정의했다:

> "A module should be responsible to one, and only one, actor."
> "모듈은 하나의, 오직 하나의 **액터**(actor)에게만 책임져야 한다."

여기서 **액터**란 해당 코드의 변경을 요청하는 사람 또는 그룹이다.
"책임"이란 특정 액터의 요구를 충족시키는 기능의 집합이며,
SRP를 지킨 클래스는 응집도 7단계 중 최고 수준인 **기능적 응집도**를 가진다.

관심사 분리(SoC)는 SRP를 레이어/모듈/시스템 수준으로 확장한 개념이다.
3계층 구조(Controller → Service → Repository)가 대표적인 적용 예시다.

### 왜 중요한가

- 서로 다른 액터의 로직이 한 클래스에 있으면, 한 액터의 변경 요청이 다른 액터의 기능을 깨뜨린다
- CFO가 급여 계산을 바꿨는데 COO의 보고서가 깨지는 사고가 대표적인 예시다
- SRP는 OCP, ISP, DIP의 전제 조건이다 — SOLID의 토대가 없으면 나머지도 무너진다
- DIP(의존성 역전)가 없으면 관심사를 분리해도 교체와 테스트가 불가능하다

### 실습 내용

| 파일 | 설명 |
|------|------|
| `code/Before.java` | SRP 위반 — Employee 클래스에 CFO/COO/CTO 3명의 액터가 섞여 있고, 공유 메서드(`getRegularHours`)로 인한 사고를 시뮬레이션한다 |
| `code/After.java` | SRP 적용 — PayCalculator, WorkReportGenerator, EmployeeRepository로 액터별 분리. 각 액터가 독립적으로 변경 가능함을 확인한다 |
| `code/GodServiceRefactoring.java` | 현업에서 가장 흔한 God Service 패턴을 인터페이스 기반으로 리팩터링. Before/After를 한 파일에서 비교 실행한다 |

### 의문점 / 더 알고 싶은 것

- 마이크로서비스에서 서비스 경계를 나누는 기준도 결국 "액터(팀)" 단위인가? (Conway의 법칙)
- 코드 중복과 SRP의 관계 — SRP를 지키기 위해 의도적으로 중복을 허용하는 경우가 있다는데, 그 판단 기준은?

### 참고 자료

- Robert C. Martin, *Clean Architecture* (2017) — SRP 재정의 원문
- Larry Constantine (1968) — 응집도/결합도 개념 원론
- [system-design-primer](https://github.com/donnemartin/system-design-primer)

---
📝 [티스토리 글 보기](https://solarchive.tistory.com/8)