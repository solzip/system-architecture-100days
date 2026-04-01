package day03_coupling_cohesion.code;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Day 3 — Before: 결합도 높고 응집도 낮은 코드
 *
 * [응집도 문제]
 * - 우연적~절차적 응집도: 검증, 해싱, 저장, 알림, 로깅이 한 메서드에 모여 있다.
 * - "이 클래스가 뭐 하는 클래스야?" → "...여러 가지요" (응집도 낮음의 신호)
 *
 * [결합도 문제]
 * - 공통 결합: 전역 상태(usersDb)를 직접 접근
 * - 내용 결합: MessageDigest 세부 구현을 직접 다룸
 * - 높은 외부 의존: SMTP 설정을 register() 안에서 직접 처리
 *
 * [SOLID 위반]
 * - SRP 위반: 변경 이유가 5가지 (검증 규칙, 해싱 알고리즘, 저장 방식, 알림 채널, 로깅 포맷)
 * - DIP 위반: 모든 것이 구체 구현에 직접 의존
 * - OCP 위반: 알림 방식을 바꾸려면 register() 메서드를 직접 수정해야 함
 */
public class Before {

    // =========================================================
    //  UserManager — 결합도 높고 응집도 낮은 "나쁜 예시"
    // =========================================================
    public static class UserManager {

        // 공통 결합: 전역 상태를 여러 곳에서 직접 접근할 수 있다
        private static Map<String, Map<String, String>> usersDb = new HashMap<>();

        public void register(String name, String email, String password) {

            // --- 유효성 검사 (UserManager의 본질적 책임이 아님) ---
            if (email == null || !email.contains("@")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (password == null || password.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters");
            }

            // --- 비밀번호 해싱 (내용 결합: 해싱 알고리즘 세부사항을 직접 다룸) ---
            String hashed;
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(password.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                hashed = sb.toString();
            } catch (Exception e) {
                throw new RuntimeException("Hashing failed", e);
            }

            // --- DB 저장 (공통 결합: 전역 Map에 직접 접근) ---
            Map<String, String> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("email", email);
            userData.put("password", hashed);
            usersDb.put(email, userData);

            // --- 알림 발송 (높은 결합: 구체적인 전송 방식에 직접 의존) ---
            // 실제라면 여기에 SMTP 설정, 세션 생성, MimeMessage 등이 들어간다.
            // 이메일 → 카카오톡으로 바꾸려면 이 메서드를 직접 수정해야 한다 (OCP 위반)
            System.out.println("[SMTP] Sending welcome email to " + email);
            System.out.println("[SMTP] Subject: Welcome " + name + "!");

            // --- 로깅 (낮은 응집도: register()와 관련 없는 관심사) ---
            System.out.println("[LOG " + java.time.LocalDateTime.now() + "] User registered: " + email);
        }

        /**
         * 이 코드의 문제를 정리하면:
         *
         * 1. register() 하나에 5가지 책임이 섞여 있다 → SRP 위반
         * 2. 해싱 알고리즘을 BCrypt로 바꾸려면 register()를 수정해야 한다 → OCP 위반
         * 3. 모든 것이 구체 구현에 직접 의존한다 → DIP 위반
         * 4. 단위 테스트가 불가능하다 → 이메일 발송 없이 register() 로직만 테스트할 방법이 없음
         * 5. 전역 상태(usersDb)를 누가 언제 바꿀지 모른다 → 디버깅 지옥
         */
    }

    public static void main(String[] args) {
        UserManager manager = new UserManager();
        manager.register("홍길동", "hong@example.com", "securepass123");
        System.out.println("--- Registered users: " + UserManager.usersDb.keySet());
    }
}