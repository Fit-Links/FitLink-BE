package spring.fitlinkbe.support.utils;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;

public class MessageConvertUtils {

    /**
     * DTO에서 값을 검증할 때 메시지를 설정했을 경우 그 메시지만 나오게 변환
     * @param e MethodArgumentNotValidException
     * @return 내가 설정한 메시지
     */
    public static String getErrorCustomMessage(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.collectingAndThen(Collectors.joining(", "),
                        msg -> msg.isEmpty() ? "보내는 파라미터를 다시 확인해주세요." : msg));
    }
}
