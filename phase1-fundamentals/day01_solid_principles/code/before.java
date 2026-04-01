package day01_solid_principles.code; /**
 * Day 1 — SOLID 원칙 실습 (Before: 리팩터링 전)
 *
 * 이 코드는 SOLID 원칙을 모두 위반하고 있다.
 * 직접 타이핑하면서 "어디가 왜 나쁜지" 주석을 달아보자.
 * 그 다음 After.java와 비교한다.
 */

// ============================================================
// ❌ 문제 1: SRP 위반
// OrderService가 주문 처리, DB 저장, 이메일 발송, 할인 계산을 전부 한다.
// 변경 이유가 4가지 → 단일 책임 원칙 위반
// ============================================================

class OrderService {
    // 주문 저장 (DB 로직이 비즈니스 로직과 섞여 있음)
    public void createOrder(String customerName, String item, double price, String customerType) {

        // ❌ 문제 2: OCP 위반
        // 새 할인 정책이 추가될 때마다 이 메서드를 수정해야 한다.
        double discount;
        if (customerType.equals("vip")) {
            discount = price * 0.2;
        } else if (customerType.equals("student")) {
            discount = price * 0.1;
        } else if (customerType.equals("employee")) {
            discount = price * 0.3;
        } else {
            discount = 0;
        }

        double finalPrice = price - discount;

        // DB 저장을 직접 수행 (하드코딩)
        System.out.println("[MySQL] INSERT INTO orders VALUES ('"
                + customerName + "', '" + item + "', " + finalPrice + ")");

        // 이메일 발송도 직접 수행
        System.out.println("[SMTP] " + customerName + "님, 주문이 완료되었습니다. "
                + "상품: " + item + ", 결제 금액: " + finalPrice + "원");
    }
}


// ============================================================
// ❌ 문제 3: LSP 위반
// Bird를 상속한 Penguin이 fly()를 호출하면 터진다.
// 부모 자리에 자식을 넣었을 때 기대와 다르게 동작한다.
// ============================================================

class Bird {
    public String fly() {
        return "날고 있다";
    }

    public String eat() {
        return "먹고 있다";
    }
}

class Penguin extends Bird {
    @Override
    public String fly() {
        // 펭귄은 날 수 없는데 Bird를 상속받았기 때문에
        // fly()를 구현해야 한다 → 억지로 예외를 던짐
        throw new UnsupportedOperationException("펭귄은 날 수 없습니다!");
    }
}


// ============================================================
// ❌ 문제 4: ISP 위반
// Worker 인터페이스에 모든 메서드가 몰려 있다.
// Robot은 eat()이 필요 없는데 구현해야 한다.
// ============================================================

interface Worker {
    void work();
    void eat();
    void sleep();
}

class Human implements Worker {
    public void work()  { System.out.println("일한다"); }
    public void eat()   { System.out.println("밥 먹는다"); }
    public void sleep() { System.out.println("잔다"); }
}

class Robot implements Worker {
    public void work()  { System.out.println("일한다"); }
    public void eat()   { /* 로봇은 안 먹는데... */ }   // ❌ 빈 구현
    public void sleep() { /* 로봇은 안 자는데... */ }   // ❌ 빈 구현
}


// ============================================================
// ❌ 문제 5: DIP 위반
// NotificationSender가 구체 클래스(EmailSender)에 직접 의존한다.
// SMS로 바꾸려면 NotificationSender 코드를 수정해야 한다.
// ============================================================

class EmailSender {
    public void send(String to, String message) {
        System.out.println("[Email → " + to + "] " + message);
    }
}

class NotificationSender {
    private EmailSender emailSender = new EmailSender();  // ❌ 구체 클래스에 직접 의존

    public void notify(String to, String message) {
        emailSender.send(to, message);
    }
}


// ============================================================
// 실행
// ============================================================

public class Before {
    public static void main(String[] args) {
        System.out.println("====== SRP + OCP 위반 ======");
        OrderService orderService = new OrderService();
        orderService.createOrder("솔", "키보드", 100000, "vip");
        orderService.createOrder("김학생", "마우스", 50000, "student");

        System.out.println();
        System.out.println("====== LSP 위반 ======");
        Bird bird = new Bird();
        System.out.println("새: " + bird.fly());
        try {
            Bird penguin = new Penguin();  // 부모 타입으로 받았는데...
            System.out.println("펭귄: " + penguin.fly());  // 💥 터진다!
        } catch (UnsupportedOperationException e) {
            System.out.println("펭귄: " + e.getMessage());
        }

        System.out.println();
        System.out.println("====== ISP 위반 ======");
        Worker human = new Human();
        human.work();
        human.eat();
        Worker robot = new Robot();
        robot.work();
        robot.eat();  // 아무 일도 안 일어남 — 인터페이스 계약 위반

        System.out.println();
        System.out.println("====== DIP 위반 ======");
        NotificationSender sender = new NotificationSender();
        sender.notify("솔", "주문이 완료되었습니다.");
    }
}