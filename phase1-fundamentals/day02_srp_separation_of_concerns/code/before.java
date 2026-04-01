package day02_srp_separation_of_concerns.code;

/**
 * SRP 위반 예시 — Employee 클래스에 3명의 액터가 섞여 있다.
 *
 * 액터 1: CFO(재무팀) → calculatePay()
 * 액터 2: COO(운영팀) → generateReport()
 * 액터 3: CTO(기술팀) → save()
 *
 * 공유 메서드 getRegularHours()가 사고의 원인이 된다.
 *
 * 컴파일: javac Before.java
 * 실행:   java Before
 */
public class before {

    // ================================================================
    // ❌ SRP 위반: 3명의 액터가 하나의 클래스에 의존
    // ================================================================
    static class Employee {
        private String name;
        private int totalHours;
        private int hourlyRate;

        public Employee(String name, int totalHours, int hourlyRate) {
            this.name = name;
            this.totalHours = totalHours;
            this.hourlyRate = hourlyRate;
        }

        // --- 액터 1: CFO(재무팀) --- "급여 계산 방식을 바꿔주세요"
        public int calculatePay() {
            int regularHours = getRegularHours();
            int overtimeHours = Math.max(0, totalHours - regularHours);
            return (regularHours * hourlyRate) + (overtimeHours * hourlyRate * 2);
        }

        // --- 액터 2: COO(운영팀) --- "보고서 형식을 바꿔주세요"
        public String generateReport() {
            int regularHours = getRegularHours();
            int overtimeHours = Math.max(0, totalHours - regularHours);
            return String.format("[보고서] %s: 정규 %d시간 / 초과 %d시간",
                    name, regularHours, overtimeHours);
        }

        // --- 액터 3: CTO(기술팀) --- "DB 스키마를 바꿉니다"
        public String save() {
            return String.format(
                    "INSERT INTO employees (name, hours, rate) VALUES ('%s', %d, %d)",
                    name, totalHours, hourlyRate);
        }

        // --- ⚠️ 위험: 여러 액터의 메서드가 공유하는 private 메서드 ---
        private int getRegularHours() {
            return Math.min(totalHours, 8);
        }
    }

    // ================================================================
    // 사고 시나리오 시뮬레이션
    // ================================================================
    public static void main(String[] args) {
        Employee emp = new Employee("김개발", 10, 20000);

        System.out.println("=== SRP 위반 코드 (Before) ===");
        System.out.println();

        // 변경 전 상태
        System.out.println("--- 변경 전 (getRegularHours = 8시간 기준) ---");
        System.out.printf("급여: %,d원%n", emp.calculatePay());
        System.out.println(emp.generateReport());
        System.out.println("DB : " + emp.save());

        // 사고 시나리오
        System.out.println();
        System.out.println("--- 사고 시나리오 ---");
        System.out.println("CFO 요청: '정규 근무 시간을 8시간에서 9시간으로 바꿔주세요'");
        System.out.println("개발자가 getRegularHours()의 8을 9로 수정했다.");
        System.out.println();
        System.out.println("문제: getRegularHours()는 generateReport()에서도 쓰인다!");
        System.out.println("COO는 변경을 요청한 적이 없는데, 보고서 숫자가 바뀐다.");

        // 변경 후 시뮬레이션
        System.out.println();
        System.out.println("--- 변경 후 (getRegularHours = 9시간 기준) ---");
        EmployeeAfterChange empV2 = new EmployeeAfterChange("김개발", 10, 20000);
        System.out.printf("급여:   %,d원 (CFO 의도대로 변경 ✅)%n", empV2.calculatePay());
        System.out.println(empV2.generateReport() + " (COO는 요청 안 했는데 바뀜 ❌)");

        System.out.println();
        System.out.println("⚠️ 핵심: 서로 다른 액터의 로직이 같은 클래스에 있으면,");
        System.out.println("  한 액터의 요청이 다른 액터의 기능을 깨뜨린다.");
    }

    // CFO 요청으로 정규 시간이 9시간으로 바뀐 버전
    static class EmployeeAfterChange {
        private String name;
        private int totalHours;
        private int hourlyRate;

        public EmployeeAfterChange(String name, int totalHours, int hourlyRate) {
            this.name = name;
            this.totalHours = totalHours;
            this.hourlyRate = hourlyRate;
        }

        public int calculatePay() {
            int regularHours = getRegularHours();
            int overtimeHours = Math.max(0, totalHours - regularHours);
            return (regularHours * hourlyRate) + (overtimeHours * hourlyRate * 2);
        }

        public String generateReport() {
            int regularHours = getRegularHours();
            int overtimeHours = Math.max(0, totalHours - regularHours);
            return String.format("[보고서] %s: 정규 %d시간 / 초과 %d시간",
                    name, regularHours, overtimeHours);
        }

        // 8 → 9로 변경됨. 급여와 보고서 모두 영향받는다.
        private int getRegularHours() {
            return Math.min(totalHours, 9);
        }
    }
}