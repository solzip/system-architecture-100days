package day03_coupling_cohesion.code;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Day 3 — After: 높은 응집도 + 낮은 결합도
 *
 * Before.java의 UserManager를 다음 기준으로 분리했다:
 *
 * [응집도 개선] 우연적 → 기능적
 * - 각 클래스가 "한 가지 일"만 한다 (기능적 응집도)
 * - 클래스 이름만 보면 뭐 하는지 바로 안다
 *
 * [결합도 개선] 내용/공통 결합 → 데이터 결합
 * - 모든 의존이 인터페이스를 통과한다 (DIP)
 * - 구현을 교체해도 UserService는 수정 불필요 (OCP)
 *
 * [SOLID 매핑]
 * - SRP: 각 클래스의 변경 이유가 1가지
 * - OCP: 새 구현 클래스를 추가하면 됨 (기존 코드 수정 없음)
 * - DIP: UserService → 인터페이스에 의존, 구체 구현은 생성자 주입
 */
public class After {

    // =============================================================
    //  1. 인터페이스 (추상화 계층) — 결합의 방향을 잡아주는 계약
    // =============================================================

    public interface UserValidator {
        void validate(String email, String password);
    }

    public interface PasswordHasher {
        String hash(String rawPassword);
    }

    public interface UserRepository {
        void save(String email, String name, String hashedPassword);
        boolean existsByEmail(String email);
    }

    public interface NotificationSender {
        void sendWelcome(String email, String name);
    }

    // =============================================================
    //  2. 구현 클래스 — 각각 기능적 응집도
    // =============================================================

    /**
     * 검증만 담당한다.
     * 변경 이유: 검증 규칙이 바뀔 때 (예: 비밀번호 최소 길이 변경)
     */
    public static class BasicUserValidator implements UserValidator {

        @Override
        public void validate(String email, String password) {
            if (email == null || !email.contains("@")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (password == null || password.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters");
            }
        }
    }

    /**
     * 비밀번호 해싱만 담당한다.
     * 변경 이유: 해싱 알고리즘이 바뀔 때 (SHA-256 → BCrypt)
     *
     * Before에서는 register() 안에 MessageDigest 코드가 직접 들어가 있었다.
     * → 내용 결합이었던 것을 별도 클래스로 캡슐화하여 데이터 결합으로 전환
     */
    public static class Sha256PasswordHasher implements PasswordHasher {

        @Override
        public String hash(String rawPassword) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(rawPassword.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (Exception e) {
                throw new RuntimeException("Hashing failed", e);
            }
        }
    }

    /**
     * 사용자 저장만 담당한다.
     * 변경 이유: 저장 방식이 바뀔 때 (InMemory → MySQL → MongoDB)
     *
     * Before에서는 전역 static Map이었다 (공통 결합).
     * → 인스턴스 필드로 바꿔서 공통 결합 제거
     */
    public static class InMemoryUserRepository implements UserRepository {

        private final Map<String, Map<String, String>> store = new HashMap<>();

        @Override
        public void save(String email, String name, String hashedPassword) {
            Map<String, String> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("password", hashedPassword);
            store.put(email, userData);
        }

        @Override
        public boolean existsByEmail(String email) {
            return store.containsKey(email);
        }

        // 테스트/디버깅용
        public Map<String, Map<String, String>> getStore() {
            return store;
        }
    }

    /**
     * 이메일 알림 발송만 담당한다.
     * 변경 이유: 이메일 전송 방식이 바뀔 때 (SMTP 설정 변경 등)
     *
     * Before에서는 SMTP 설정이 register() 안에 하드코딩되어 있었다.
     * → 이메일 세부사항이 이 클래스 안에 캡슐화됨
     */
    public static class EmailNotificationSender implements NotificationSender {

        private final String smtpHost;
        private final int smtpPort;
        private final String fromAddress;

        public EmailNotificationSender(String smtpHost, int smtpPort, String fromAddress) {
            this.smtpHost = smtpHost;
            this.smtpPort = smtpPort;
            this.fromAddress = fromAddress;
        }

        @Override
        public void sendWelcome(String email, String name) {
            // 실제라면 여기서 JavaMail API 사용
            System.out.println("[Email] " + fromAddress + " → " + email);
            System.out.println("[Email] Welcome " + name + "!");
        }
    }

    /**
     * 카카오톡 알림 발송만 담당한다.
     * 변경 이유: 카카오 API 스펙이 바뀔 때
     *
     * OCP 증명: 새 알림 방식이 필요할 때 "기존 코드 수정 없이 클래스 추가만" 하면 된다.
     * UserService는 NotificationSender 인터페이스에만 의존하므로 한 줄도 수정하지 않는다.
     */
    public static class KakaoNotificationSender implements NotificationSender {

        private final String apiKey;

        public KakaoNotificationSender(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public void sendWelcome(String email, String name) {
            System.out.println("[KakaoTalk] API Key: " + apiKey);
            System.out.println("[KakaoTalk] " + name + "님, 가입을 환영합니다!");
        }
    }

    // =============================================================
    //  3. 오케스트레이터 — 조합만 담당
    // =============================================================

    /**
     * UserService는 "어떻게"를 모르고, "무엇을 어떤 순서로"만 안다.
     *
     * 결합도 분석:
     * - Before: 내용 결합 + 공통 결합 (세부 구현을 직접 다룸)
     * - After:  데이터 결합 (인터페이스를 통해 필요한 값만 전달)
     *
     * 교체 용이성:
     * - 해싱을 BCrypt로?  → new BcryptPasswordHasher() 주입
     * - 이메일 대신 카카오? → new KakaoNotificationSender() 주입
     * - DB를 MySQL로?     → new MysqlUserRepository() 주입
     * → UserService 코드는 한 줄도 수정하지 않는다 (OCP 달성)
     */
    public static class UserService {

        private final UserValidator validator;
        private final PasswordHasher hasher;
        private final UserRepository repository;
        private final NotificationSender notification;

        public UserService(UserValidator validator,
                           PasswordHasher hasher,
                           UserRepository repository,
                           NotificationSender notification) {
            this.validator = validator;
            this.hasher = hasher;
            this.repository = repository;
            this.notification = notification;
        }

        public void register(String name, String email, String password) {
            validator.validate(email, password);
            String hashed = hasher.hash(password);
            repository.save(email, name, hashed);
            notification.sendWelcome(email, name);
        }
    }

    // =============================================================
    //  4. 조립 (Composition Root) — 의존성을 연결하는 곳
    // =============================================================

    public static void main(String[] args) {

        // --- 시나리오 1: 이메일 알림으로 회원가입 ---
        System.out.println("=== 시나리오 1: 이메일 알림 ===");

        UserValidator validator = new BasicUserValidator();
        PasswordHasher hasher = new Sha256PasswordHasher();
        InMemoryUserRepository repository = new InMemoryUserRepository();
        NotificationSender emailSender = new EmailNotificationSender(
                "smtp.gmail.com", 587, "admin@example.com"
        );

        // UserService는 인터페이스에만 의존한다 (DIP)
        UserService service = new UserService(validator, hasher, repository, emailSender);
        service.register("홍길동", "hong@example.com", "securepass123");

        System.out.println("--- Registered users: " + repository.getStore().keySet());
        System.out.println();

        // --- 시나리오 2: 카카오톡 알림으로 교체 ---
        // UserService 코드 수정 없이 구현체만 교체하면 된다 (OCP)
        System.out.println("=== 시나리오 2: 카카오톡 알림 (교체) ===");

        NotificationSender kakaoSender = new KakaoNotificationSender("fake-api-key");
        UserService serviceWithKakao = new UserService(validator, hasher, repository, kakaoSender);
        serviceWithKakao.register("김철수", "kim@example.com", "anotherpass99");

        System.out.println("--- Registered users: " + repository.getStore().keySet());
    }
}