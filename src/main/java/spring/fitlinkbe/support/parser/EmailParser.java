package spring.fitlinkbe.support.parser;

import lombok.extern.slf4j.Slf4j;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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

        String[] headerAndBody = splitHeadersAndBody(decodedContent);
        String boundaryText = findBoundaryText(headerAndBody[0]);
        List<String> parts = splitParts(headerAndBody[1], boundaryText);

        String token = extractTokenFromParts(parts);
        if (token != null) {
            return token;
        }

        log.warn("Token not found in email content: {}", decodedContent);
        throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
    }

    /**
     * multipart 형식의 본문에서 토큰을 추출하는 메서드
     */
    private static String extractTokenFromParts(List<String> parts) {
        for (String part : parts) {
            String token = extractTokenFromPart(part);
            if (token != null) {
                return token;
            }
        }
        return null;
    }

    /**
     * 본문에서 각 파트에서 토큰을 추출하는 메서드
     */
    private static String extractTokenFromPart(String part) {
        List<String> headers = new ArrayList<>();
        String[] lines = part.split("\r\n");

        int blankIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                blankIndex = i;
                break;
            }
            headers.add(lines[i]);
        }

        // bodyContent를 빈 줄 바로 '다음 줄'로 설정
        String bodyContent = "";
        if (blankIndex != -1 && blankIndex + 1 < lines.length) {
            bodyContent = lines[blankIndex + 1];
        }

        String body = decodeBodyIfEncoded(bodyContent, headers);

        Pattern pattern = Pattern.compile("\\[[^]]+]\\s*([A-Za-z0-9]+)");
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static String decodeBodyIfEncoded(String bodyContent, List<String> headers) {
        String encoding = headers.stream()
                .filter(h -> h.toLowerCase().startsWith("content-transfer-encoding:"))
                .map(h -> h.substring(h.indexOf(":") + 1).trim().toLowerCase())
                .findFirst().orElse(null);

        if (encoding == null) {
            return bodyContent;
        }

        return switch (encoding) {
            case "base64" -> {
                byte[] decodedBytes = Base64.getDecoder().decode(bodyContent);
                yield new String(decodedBytes, StandardCharsets.UTF_8);
            }
            default -> bodyContent;
        };
    }


    /**
     * 본문을 boundary text로 나누는 메서드
     *
     * @param bodyContent  본문 내용
     * @param boundaryText boundary text
     * @return 나누어진 본문 리스트, multipart 형식이 아닌 경우 본문 내용 1개만 반환
     */
    private static List<String> splitParts(String bodyContent, String boundaryText) {
        if (boundaryText == null) {
            // boundary text가 없으면 본문을 그대로 반환
            List<String> singlePart = new ArrayList<>();
            singlePart.add(bodyContent);
            return singlePart;
        }

        List<String> parts = new ArrayList<>();
        String[] splitParts = bodyContent.split("--" + boundaryText);

        for (String part : splitParts) {
            if (!part.trim().isEmpty() && !part.trim().equals("--")) {
                parts.add(part.trim());
            }
        }

        return parts;
    }

    /**
     * 헤더에서 boundary text를 찾는 메서드
     *
     * @return boundary text
     */
    private static String findBoundaryText(String headerContent) {
        // 정규 표현식 패턴: "boundary="로 시작하는 부분
        Pattern pattern = Pattern.compile("boundary=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(headerContent);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * message header 와 body를 분리하는 메서드
     *
     * @param content 전체 MIME 텍스트 (Base64 디코딩 직후)
     * @return String[0] = headers, String[1] = body
     */
    private static String[] splitHeadersAndBody(String content) {
        // 1) 표준 CRLF 구분자(\r\n\r\n) 위치 찾기
        int idx = content.indexOf("\r\n\r\n");
        // 2) 혹시 CRLF가 아닌 LF만 쓰인 경우 대비
        if (idx < 0) {
            idx = content.indexOf("\n\n");
        }
        if (idx < 0) {
            // 구분자 자체가 없으면 전체를 headers로 보고 body는 빈 문자열
            return new String[]{content, ""};
        }

        String headers = content.substring(0, idx);
        String body = content.substring(idx + (content.startsWith("\r\n\r\n", idx) ? 4 : 2));
        return new String[]{headers, body};
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
