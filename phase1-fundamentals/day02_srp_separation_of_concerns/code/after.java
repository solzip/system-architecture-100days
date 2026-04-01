package day02_srp_separation_of_concerns.code;

/**
 * SRP 적용 예시 — 각 클래스가 하나의 액터에게만 책임진다.
 *
 * PayCalculator       → CFO(재무팀)
 * WorkReportGenerator → COO(운영팀)
 * EmployeeRepository  → CTO(기술팀)
 * Employee            → 순수 데이터
 *
 * 컴파일: javac After.java
 * 실행:   java After
 */
public class after {

    // ================================================================
    // 도메인 모델 — 순수한 데이터
    // ================================================================
    static class Employee {
        private final String name;
        private final int totalHours;
        private final int hourlyRate;

        public Employee(String name, int totalHours, int hourlyRate) {
            this.name = name;
            this.totalHours = totalHours;
            this.hourlyRate = hourlyRate;
        }

        public String getName()     { return name; }
        public int getTotalHours()  { return totalHours; }
        public int getHourlyRate()  { return hourlyRate; }
    }

    // ================================================================
    // 액터 1: CFO(재무팀) — 급여 계산만 담당
    // 변경 이유: 급여 정책 변경
    // ================================================================
    static class PayCalculator {
        private final int regularHourLimit;

        public PayCalculator(int regularHourLimit) {
            this.regularHourLimit = regularHourLimit;
        }

        public int calculatePay(Employee emp) {
            int regular  = Math.min(emp.getTotalHours(), regularHourLimit);
            int overtime = Math.max(0, emp.getTotalHours() - regularHourLimit);
            return (regular * emp.getHourlyRate()) + (overtime * emp.getHourlyRate() * 2);
        }
    }

    // ================================================================
    // 액터 2: COO(운영팀) — 보고서 생성만 담당
    // 변경 이유: 보고서 항목/형식 변경
    // ================================================================
    static class WorkReportGenerator {
        private final int standardHours;

        public WorkReportGenerator(int standardHours) {
            this.standardHours = standardHours;
        }

        public String generate(Employee emp) {
            int regular  = Math.min(emp.getTotalHours(), standardHours);
            int overtime = Math.max(0, emp.getTotalHours() - standardHours);
            return String.format("[보고서] %s: 정규 %d시간 / 초과 %d시간",
                    emp.getName(), regular, overtime);
        }
    }

    // ================================================================
    // 액터 3: CTO(기술팀) — DB 저장만 담당
    // 변경 이유: DB 스키마/엔진 변경
    // ================================================================
    static class EmployeeRepository {
        public String save(Employee emp) {
            return String.format(
                    "INSERT INTO employees (name, hours, rate) VALUES ('%s', %d, %d)",
                    emp.getName(), emp.getTotalHours(), emp.getHourlyRate());
        }
    }

    // ================================================================
    // 실행 — Before와 비교
    // ================================================================
    public static void main(String[] args) {
        Employee emp = new Employee("김개발", 10, 20000);

        System.out.println("=== SRP 적용 코드 (After) ===");
        System.out.println();

        // 변경 전: 급여, 보고서 모두 8시간 기준
        System.out.println("--- 변경 전 (급여=8시간, 보고서=8시간) ---");
        PayCalculator payV1 = new PayCalculator(8);
        WorkReportGenerator reportV1 = new WorkReportGenerator(8);
        EmployeeRepository repo = new EmployeeRepository();

        System.out.printf("급여: %,d원%n", payV1.calculatePay(emp));
        System.out.println(reportV1.generate(emp));
        System.out.println("DB : " + repo.save(emp));

        // CFO 요청: 급여 기준 9시간으로 변경
        System.out.println();
        System.out.println("--- CFO 요청: 급여 기준을 9시간으로 변경 ---");
        PayCalculator payV2 = new PayCalculator(9);

        System.out.printf("급여: %,d원 (CFO 요청 반영 ✅)%n", payV2.calculatePay(emp));
        System.out.println(reportV1.generate(emp) + " (COO 보고서 영향 없음 ✅)");

        // COO 요청: 보고서 기준 독립적으로 변경
        System.out.println();
        System.out.println("--- COO 요청: 보고서 기준을 7시간으로 변경 ---");
        WorkReportGenerator reportV2 = new WorkReportGenerator(7);

        System.out.printf("급여: %,d원 (급여는 여전히 9시간 기준 ✅)%n", payV2.calculatePay(emp));
        System.out.println(reportV2.generate(emp) + " (보고서만 독립 변경 ✅)");

        // 요약
        System.out.println();
        System.out.println("=".repeat(55));
        System.out.println("핵심 비교");
        System.out.println("=".repeat(55));
        System.out.println();
        System.out.println("Before: getRegularHours() 하나를 바꾸면 급여+보고서 동시 변경");
        System.out.println("After:  PayCalculator와 WorkReportGenerator가 각자 기준값을 가짐");
        System.out.println("       → 한 액터의 변경이 다른 액터에 영향 없음");
    }
}