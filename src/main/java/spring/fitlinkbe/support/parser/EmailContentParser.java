package spring.fitlinkbe.support.parser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailContentParser {

    /**
     * 이메일 컨텐츠를 파싱해서 본문만 추출하는 메서드
     *
     * @return 본문 내용 content 찾지 못하면 null 반환
     */
    public static String parseEmailContent(String encodedContent) {
        // email 본문 Base64 디코드
        byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
        String decodedContent = new String(decodedBytes, StandardCharsets.UTF_8);

        // 정규표현식: "Content-Type: text/plain" 이후에 빈 줄을 찾고, 그 다음 줄의 첫 번째 non-empty 텍스트를 캡처
        Pattern pattern = Pattern.compile("(?s)Content-Type:\\s*text/plain.*?\\r?\\n\\r?\\n\\s*([^\\r\\n]+)");
        Matcher matcher = pattern.matcher(decodedContent);

        if (matcher.find()) {
            String extracted = matcher.group(1).trim();
            return extracted;
        }

        return null; // 본문을 찾지 못한 경우 null 반환
    }
}
