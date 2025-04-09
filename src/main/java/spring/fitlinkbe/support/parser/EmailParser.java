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

        /*
         * 정규표현식 설명:
         *  - (?s) 플래그로 DOTALL 모드를 활성화하여 개행문자도 '.'에 포함
         *  - "Content-Type:"으로 시작해서 "text/plain"을 포함하는 부분을 찾은 후,
         *    헤더와 본문 사이의 빈 줄(\r?\n\r?\n)을 찾음.
         *  - 그 뒤 이어서 공백 후 대괄호로 감싼 라인(\[[^\]]*\])이 나오고,
         *    그 다음 줄의 첫 번째 non-empty 라인을 캡처하여 토큰으로 반환.
         */
        Pattern pattern = Pattern.compile(
                "(?s)Content-Type:\\s*text/plain.*?\\r?\\n\\r?\\n\\s*\\[[^\\]]*\\]\\s*\\r?\\n\\s*([^\\r\\n]+)"
        );
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
