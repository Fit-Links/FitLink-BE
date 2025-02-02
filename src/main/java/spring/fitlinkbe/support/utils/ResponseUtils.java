package spring.fitlinkbe.support.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseUtils {
    public static void setErrorResponse(HttpServletResponse response, HttpStatus status) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResultResponse<Object> apiResultResponse;
        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            response.setStatus(status.value());
            apiResultResponse = ApiResultResponse.of(status, false, status.getReasonPhrase(), null);
        } else {
            response.setStatus(HttpStatus.OK.value());
            apiResultResponse = ApiResultResponse.of(status, false, status.getReasonPhrase(), null);
        }

        try {
            response.getWriter().write(objectMapper.writeValueAsString(apiResultResponse));
        } catch (IOException e) {
            log.error("Error while writing response", e);
        }
    }

    public static void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) {
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ApiResultResponse<Object> apiResultResponse;
        if (errorCode.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            apiResultResponse = ApiResultResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, false, errorCode.getMsg(), null);
        } else {
            response.setStatus(HttpStatus.OK.value());
            apiResultResponse = ApiResultResponse.of(HttpStatus.valueOf(errorCode.getStatus()), false, errorCode.getMsg(), null);
        }

        try {
            response.getWriter().write(objectMapper.writeValueAsString(apiResultResponse));
        } catch (IOException e) {
            log.error("Error while writing response", e);
        }
    }
}
