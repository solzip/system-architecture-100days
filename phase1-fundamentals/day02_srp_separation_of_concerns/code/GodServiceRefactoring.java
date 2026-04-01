package day02_srp_separation_of_concerns.code;

import java.util.*;

/**
 * 실전 SRP 예시 — God Service 리팩터링
 *
 * 현업에서 가장 자주 보이는 SRP 위반 패턴인 "God Service"를
 * 액터 기준으로 분리하는 과정을 보여준다.
 *
 * 컴파일: javac GodServiceRefactoring.java
 * 실행:   java GodServiceRefactoring
 */
public class GodServiceRefactoring {

    // ================================================================
    // 공통 모델
    // ================================================================
    static class Order {
        final String id;
        final String userId;
        final String product;
        final int price;
        final int quantity;
        final int total;
        String status;

        Order(String id, String userId, String product, int price, int quantity) {
            this.id = id;
            this.userId = userId;
            this.product = product;
            this.price = price;
            this.quantity = quantity;
            this.total = price * quantity;
            this.status = "created";
        }

        @Override
        public String toString() {
            return String.format("Order{id=%s, product=%s, total=%,d원, status=%s}",
                    id, product, total, status);
        }
    }

    // ================================================================
    // ❌ BEFORE: God Service — 5명의 액터가 하나의 클래스에
    // ================================================================
    static class OrderServiceBefore {
        private final Map<String, Order> db = new HashMap<>();
        private final List<String> logs = new ArrayList<>();
        private int seq = 0;

        // 액터: 기획팀 — "할인 정책 바꿔주세요"
        public Order createOrder(String userId, String product, int price, int qty) {
            if (qty > 100) throw new RuntimeException("100개 초과 불가");
            if (price * qty > 1_000_000) throw new RuntimeException("금액 초과");

            Order order = new Order("ORD-" + (++seq), userId, product, price, qty);
            db.put(order.id, order);

            // 액터: 마케팅팀 — "이메일 템플릿 바꿔주세요"
            sendEmail(userId, "주문 확인: " + order.id);

            // 액터: 물류팀 — "재고 연동 방식 바꿔주세요"
            updateInventory(product, qty);

            // 액터: 데이터팀 — "로그 형식 바꿔주세요"
            log("주문 생성: " + order.id + " / " + product + " / " + order.total + "원");

            return order;
        }

        // 액터: 경영팀 — "보고서 항목 추가해주세요"
        public String generateSalesReport() {
            int totalRevenue = db.values().stream().mapToInt(o -> o.total).sum();
            return String.format("매출 보고서: 총 %d건, 총액 %,d원", db.size(), totalRevenue);
        }

        // 액터: IT팀 — "ERP 연동 API 버전 올려주세요"
        public String syncToERP(String orderId) {
            Order order = db.get(orderId);
            return "ERP 동기화: " + order.id + " → SAP";
        }

        private void sendEmail(String userId, String msg) {
            logs.add("[EMAIL] " + userId + ": " + msg);
        }

        private void updateInventory(String product, int qty) {
            logs.add("[INVENTORY] " + product + " -" + qty);
        }

        private void log(String msg) {
            logs.add("[LOG] " + msg);
        }

        public List<String> getLogs() { return logs; }
    }

    // ================================================================
    // ✅ AFTER: 액터별로 분리
    // ================================================================

    // --- 포트 (인터페이스) ---
    interface OrderRepository {
        Order save(Order order);
        Order findById(String id);
        List<Order> findAll();
    }

    interface NotificationService {
        void sendOrderConfirmation(String userId, String orderId);
        List<String> getSentLog();
    }

    interface InventoryService {
        void reserve(String product, int quantity);
        List<String> getReserveLog();
    }

    interface AuditLogger {
        void log(String event, String detail);
        List<String> getLogs();
    }

    // --- 액터 1: 기획팀 → 주문 비즈니스 로직 ---
    static class OrderServiceAfter {
        private final OrderRepository repository;
        private final NotificationService notification;
        private final InventoryService inventory;
        private final AuditLogger auditLogger;
        private int seq = 0;

        OrderServiceAfter(OrderRepository repo, NotificationService noti,
                          InventoryService inv, AuditLogger audit) {
            this.repository = repo;
            this.notification = noti;
            this.inventory = inv;
            this.auditLogger = audit;
        }

        public Order createOrder(String userId, String product, int price, int qty) {
            if (qty > 100) throw new RuntimeException("100개 초과 불가");
            if (price * qty > 1_000_000) throw new RuntimeException("금액 초과");

            Order order = new Order("ORD-" + (++seq), userId, product, price, qty);
            repository.save(order);
            notification.sendOrderConfirmation(userId, order.id);
            inventory.reserve(product, qty);
            auditLogger.log("ORDER_CREATED", order.id + " / " + order.total + "원");
            return order;
        }
    }

    // --- 액터 2: 경영팀 → 보고서 ---
    static class SalesReportService {
        private final OrderRepository repository;

        SalesReportService(OrderRepository repo) { this.repository = repo; }

        public String generateReport() {
            List<Order> orders = repository.findAll();
            int total = orders.stream().mapToInt(o -> o.total).sum();
            return String.format("매출 보고서: 총 %d건, 총액 %,d원", orders.size(), total);
        }
    }

    // --- 액터 3: IT팀 → ERP 연동 ---
    static class ERPSyncService {
        private final OrderRepository repository;

        ERPSyncService(OrderRepository repo) { this.repository = repo; }

        public String sync(String orderId) {
            Order order = repository.findById(orderId);
            return "ERP 동기화: " + order.id + " → SAP";
        }
    }

    // --- 어댑터 구현체 ---
    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<String, Order> storage = new HashMap<>();

        public Order save(Order order) { storage.put(order.id, order); return order; }
        public Order findById(String id) { return storage.get(id); }
        public List<Order> findAll() { return new ArrayList<>(storage.values()); }
    }

    static class ConsoleNotification implements NotificationService {
        private final List<String> sent = new ArrayList<>();

        public void sendOrderConfirmation(String userId, String orderId) {
            sent.add("[EMAIL] " + userId + ": 주문 확인 " + orderId);
        }

        public List<String> getSentLog() { return sent; }
    }

    static class SimpleInventoryService implements InventoryService {
        private final List<String> reserved = new ArrayList<>();

        public void reserve(String product, int qty) {
            reserved.add("[INVENTORY] " + product + " -" + qty);
        }

        public List<String> getReserveLog() { return reserved; }
    }

    static class ConsoleAuditLogger implements AuditLogger {
        private final List<String> logs = new ArrayList<>();

        public void log(String event, String detail) {
            logs.add("[AUDIT] " + event + ": " + detail);
        }

        public List<String> getLogs() { return logs; }
    }

    // ================================================================
    // 실행 — Before/After 비교
    // ================================================================
    public static void main(String[] args) {
        String sep = "=".repeat(60);

        // --- BEFORE ---
        System.out.println(sep);
        System.out.println("BEFORE: God Service (5명의 액터가 하나의 클래스에)");
        System.out.println(sep);

        OrderServiceBefore godService = new OrderServiceBefore();
        Order o1 = godService.createOrder("user_1", "키보드", 50000, 3);
        System.out.println("주문:   " + o1);
        System.out.println("보고서: " + godService.generateSalesReport());
        System.out.println("ERP:    " + godService.syncToERP(o1.id));

        System.out.println("\n부작용 로그 (전부 OrderService 안에서 발생):");
        godService.getLogs().forEach(l -> System.out.println("  " + l));

        System.out.println("\n문제점:");
        System.out.println("  - 마케팅팀이 이메일 템플릿 바꾸려면 → OrderService 수정");
        System.out.println("  - 물류팀이 재고 연동 바꾸려면     → OrderService 수정");
        System.out.println("  - 5명의 액터가 같은 파일을 건드림  → Git 충돌, 사이드이펙트");

        // --- AFTER ---
        System.out.println("\n" + sep);
        System.out.println("AFTER: 액터별 분리");
        System.out.println(sep);

        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        ConsoleNotification noti = new ConsoleNotification();
        SimpleInventoryService inv = new SimpleInventoryService();
        ConsoleAuditLogger audit = new ConsoleAuditLogger();

        OrderServiceAfter orderService = new OrderServiceAfter(repo, noti, inv, audit);
        SalesReportService reportService = new SalesReportService(repo);
        ERPSyncService erpService = new ERPSyncService(repo);

        Order o2 = orderService.createOrder("user_1", "키보드", 50000, 3);
        System.out.println("주문:   " + o2);
        System.out.println("보고서: " + reportService.generateReport());
        System.out.println("ERP:    " + erpService.sync(o2.id));

        System.out.println("\n각 서비스의 로그 (독립적으로 관리):");
        noti.getSentLog().forEach(l -> System.out.println("  " + l));
        inv.getReserveLog().forEach(l -> System.out.println("  " + l));
        audit.getLogs().forEach(l -> System.out.println("  " + l));

        // --- 변경 시나리오 비교 ---
        System.out.println("\n" + sep);
        System.out.println("변경 시나리오 비교");
        System.out.println(sep);

        System.out.println("\n요청: '이메일 템플릿을 HTML로 바꿔주세요' (마케팅팀)");
        System.out.println("  Before → OrderServiceBefore.java 수정 (600줄짜리 파일)");
        System.out.println("  After  → ConsoleNotification.java만 수정 (알림 로직만 있음)");

        System.out.println("\n요청: '매출 보고서에 평균 금액 추가해주세요' (경영팀)");
        System.out.println("  Before → OrderServiceBefore.java 수정 (또 같은 파일)");
        System.out.println("  After  → SalesReportService.java만 수정 (보고서 로직만 있음)");

        System.out.println("\n요청: 'ERP API 버전 올려주세요' (IT팀)");
        System.out.println("  Before → OrderServiceBefore.java 수정 (또...)");
        System.out.println("  After  → ERPSyncService.java만 수정 (연동 로직만 있음)");
    }
}