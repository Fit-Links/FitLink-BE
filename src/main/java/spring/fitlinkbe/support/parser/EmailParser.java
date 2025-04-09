package spring.fitlinkbe.support.parser;

import lombok.extern.slf4j.Slf4j;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class EmailParser {

    /**
     * 이메일 컨텐츠를 파싱해서 본문만 추출하는 메서드
     *
     * @return 본문 내용 content
     * @throws CustomException 토큰을 찾을 수 없는 경우
     */
    public static String parseEmailContent(String encodedContent) {
        // email 본문 Base64 디코드
        byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
        String decodedContent = new String(decodedBytes, StandardCharsets.UTF_8);

        // 정규표현식: "Content-Type: text/plain" 이후에 빈 줄을 찾고, 그 다음 줄의 첫 번째 non-empty 텍스트를 캡처
        Pattern pattern = Pattern.compile("(?s)Content-Type:\\s*text/plain.*?\\r?\\n\\r?\\n\\s*([^\\r\\n]+)");
        Matcher matcher = pattern.matcher(decodedContent);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        log.warn("Token not found in email content: {}", decodedContent);
        throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
    }

    /**
     * email 발신자 도메인에서 @ 앞 전화번호 추출 (01092921231@naver.com)
     *
     * @param source 이메일 발신자 도메인
     * @throws CustomException 전화번호를 찾을 수 없는 경우
     */
    public static String extractPhoneNumber(String source) {
        // 정규 표현식 패턴: "@" 앞의 숫자 10개 또는 11개
        Pattern pattern = Pattern.compile("(\\d{10,11})@");
        Matcher matcher = pattern.matcher(source);

        if (matcher.find()) {
            return matcher.group(1);
        }

        log.warn("Phone number not found in email sender domain: {}", source);
        throw new CustomException(ErrorCode.PHONE_NUMBER_NOT_FOUND);
    }
}
