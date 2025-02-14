package spring.fitlinkbe.support.utils;

import java.security.SecureRandom;

public class RandomStringGenerator {
    // 사용할 문자셋 (대문자, 소문자, 숫자)
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 지정한 길이의 랜덤 문자열을 생성합니다.
     *
     * @return 랜덤 문자열
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}

