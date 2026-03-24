/**
 * Day 1 — SOLID 원칙 실습 (After: 리팩터링 후)
 *
 * Before.java의 모든 SOLID 위반을 수정한 버전이다.
 * 각 수정 포인트에 어떤 원칙을 적용했는지 표기해두었다.
 *
 * 타이핑하면서 Before와 비교하자:
 *   - 클래스가 몇 개 늘었는가?
 *   - 대신 각 클래스가 얼마나 단순해졌는가?
 *   - 새 기능을 추가할 때 기존 코드를 건드려야 하는가?
 */

import java.util.ArrayList;
import java.util.List;

// ============================================================
// ✅ SRP 적용: 책임을 분리한다
//   - OrderRepository  → DB 저장만
//   - EmailService      → 이메일 발송만
//   - DiscountPolicy    → 할인 계산만
//   - OrderService      → 비즈니스 흐름 조율만
// ============================================================

// --- 할인 정책 (OCP 적용) ---

// ✅ OCP: 추상화를 만들고, 새 정책은 새 클래스로 추가한다.
//         기존 코드를 수정하지 않고 확장할 수 있다.
interface DiscountPolicy {
    double calculate(double price);
}

class VipDiscount implements DiscountPolicy {
    @Override
    public double calculate(double price) {
        return price * 0.2;
    }
}

class StudentDiscount implements DiscountPolicy {
    @Override
    public double calculate(double price) {
        return price * 0.1;
    }
}

class EmployeeDiscount implements DiscountPolicy {
    @Override
    public double calculate(double price) {
        return price * 0.3;
    }
}

class NoDiscount implements DiscountPolicy {
    @Override
    public double calculate(double price) {
        return 0;
    }
}

// 💡 새 할인 정책이 필요하면?
//    → 새 클래스를 추가하면 끝. if문 수정 없음.
// class MilitaryDiscount implements DiscountPolicy { ... }


// --- 저장소 (DIP 적용) ---

// ✅ DIP: 추상화(인터페이스)에 의존한다.
//         MySQL → PostgreSQL 교체 시 OrderService 수정 불필요.
interface OrderRepository {
    void save(String customerName, String item, double price);
}

class MySqlOrderRepository implements OrderRepository {
    @Override
    public void save(String customerName, String item, double price) {
        System.out.println("[MySQL] INSERT INTO orders VALUES ('"
                + customerName + "', '" + item + "', " + price + ")");
    }
}

// 교체 예시: PostgreSQL로 바꿔도 OrderService는 그대로
class PostgresOrderRepository implements OrderRepository {
    @Override
    public void save(String customerName, String item, double price) {
        System.out.println("[PostgreSQL] INSERT INTO orders VALUES ('"
                + customerName + "', '" + item + "', " + price + ")");
    }
}


// --- 알림 서비스 (SRP + DIP 적용) ---

// ✅ DIP: 알림 방식도 추상화한다.
interface NotificationService {
    void send(String to, String message);
}

class EmailNotification implements NotificationService {
    @Override
    public void send(String to, String message) {
        System.out.println("[Email → " + to + "] " + message);
    }
}

class SmsNotification implements NotificationService {
    @Override
    public void send(String to, String message) {
        System.out.println("[SMS → " + to + "] " + message);
    }
}


// --- 주문 서비스 (SRP 적용) ---

// ✅ SRP: OrderService는 "주문 흐름을 조율"하는 것만 한다.
//         저장, 할인, 알림은 각각 다른 객체에 위임한다.
// ✅ DIP: 구체 클래스가 아닌 인터페이스에 의존한다.
//         생성자로 주입받는다 (Dependency Injection).
class OrderService {
    private final OrderRepository repository;
    private final NotificationService notification;

    // 생성자 주입 — 구체 구현을 외부에서 결정한다
    public OrderService(OrderRepository repository, NotificationService notification) {
        this.repository = repository;
        this.notification = notification;
    }

    public void createOrder(String customerName, String item, double price, DiscountPolicy policy) {
        double discount = policy.calculate(price);
        double finalPrice = price - discount;

        repository.save(customerName, item, finalPrice);
        notification.send(customerName,
                "주문 완료! 상품: " + item + ", 결제 금액: " + finalPrice + "원");
    }
}


// ============================================================
// ✅ LSP 적용: 상속 구조를 재설계한다
//   - "새 = 날 수 있다"는 가정이 잘못이었다.
//   - 날 수 있는 능력은 별도 인터페이스로 분리한다.
// ============================================================

// ✅ ISP도 동시에 적용: 능력별로 인터페이스를 분리한다.
interface Flyable {
    String fly();
}

interface Swimmable {
    String swim();
}

interface Eatable {
    String eat();
}

// 참새: 날 수 있고, 먹을 수 있다
class Sparrow implements Flyable, Eatable {
    @Override
    public String fly() { return "참새가 날고 있다"; }

    @Override
    public String eat() { return "참새가 먹고 있다"; }
}

// 펭귄: 수영할 수 있고, 먹을 수 있다 (날 수 없음!)
// → Flyable을 구현하지 않으므로 fly()를 호출할 수 없다.
//   컴파일 단계에서 실수를 방지한다. (Before에서는 런타임에 터졌다)
class PenguinFixed implements Swimmable, Eatable {
    @Override
    public String swim() { return "펭귄이 수영하고 있다"; }

    @Override
    public String eat() { return "펭귄이 먹고 있다"; }
}


// ============================================================
// ✅ ISP 적용: Worker 인터페이스를 분리한다
// ============================================================

interface Workable {
    void work();
}

interface FeedRequired {
    void eat();
}

interface SleepRequired {
    void sleep();
}

// Human은 세 가지 다 필요
class HumanWorker implements Workable, FeedRequired, SleepRequired {
    @Override
    public void work()  { System.out.println("사람이 일한다"); }

    @Override
    public void eat()   { System.out.println("사람이 밥 먹는다"); }

    @Override
    public void sleep() { System.out.println("사람이 잔다"); }
}

// Robot은 일만 하면 된다 — 불필요한 메서드 구현이 사라졌다
class RobotWorker implements Workable {
    @Override
    public void work()  { System.out.println("로봇이 일한다"); }
}


// ============================================================
// 실행 — 조립은 한 곳에서 (Composition Root)
// ============================================================

public class After {
    public static void main(String[] args) {
        System.out.println("====== SRP + OCP + DIP 적용 ======");

        // 의존성을 여기서 조립한다 (Composition Root)
        OrderRepository repo = new MySqlOrderRepository();
        NotificationService noti = new EmailNotification();
        OrderService orderService = new OrderService(repo, noti);

        // 할인 정책을 외부에서 주입 — if문 없이 정책 교체 가능
        orderService.createOrder("솔", "키보드", 100000, new VipDiscount());
        orderService.createOrder("김학생", "마우스", 50000, new StudentDiscount());

        // 💡 DB를 PostgreSQL로 교체하고 싶다면?
        // OrderRepository repo = new PostgresOrderRepository();
        // → OrderService 코드는 한 글자도 안 바뀐다!

        System.out.println();
        System.out.println("====== LSP + ISP 적용 ======");

        Flyable sparrow = new Sparrow();
        System.out.println(sparrow.fly());  // 정상 동작

        // PenguinFixed는 Flyable이 아니므로 fly()를 호출할 수 없다.
        // 아래 주석을 해제하면 컴파일 에러가 난다 — 런타임이 아닌 컴파일 타임에 잡힌다!
        // Flyable penguin = new PenguinFixed();  // ❌ 컴파일 에러

        Swimmable penguin = new PenguinFixed();
        System.out.println(penguin.swim());  // 정상 동작

        System.out.println();
        System.out.println("====== ISP 적용 (Worker) ======");

        Workable human = new HumanWorker();
        human.work();
        ((FeedRequired) human).eat();  // Human은 FeedRequired도 구현

        Workable robot = new RobotWorker();
        robot.work();
        // robot.eat();  // ❌ 컴파일 에러 — Robot은 FeedRequired가 아니다!

        System.out.println();
        System.out.println("====== DIP 적용 (알림 교체) ======");

        // Email → SMS로 교체: 한 줄만 바꾸면 된다
        NotificationService smsNoti = new SmsNotification();
        OrderService orderService2 = new OrderService(repo, smsNoti);
        orderService2.createOrder("솔", "모니터", 300000, new VipDiscount());
    }
}