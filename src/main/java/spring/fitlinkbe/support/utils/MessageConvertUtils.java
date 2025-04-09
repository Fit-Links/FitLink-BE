package spring.fitlinkbe.support.utils;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collection;
import java.util.stream.Collectors;

public class MessageConvertUtils {

    /**
     * DTO에서 값을 검증할 때 메시지를 설정했을 경우 그 메시지만 나오게 변환
     *
     * @param e MethodArgumentNotValidException
     * @return 내가 설정한 메시지
     */
    public static String getErrorCustomMessage(Exception e) {

        if (e instanceof MethodArgumentNotValidException exception) {

            return exception.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .distinct()
                    .collect(Collectors.collectingAndThen(Collectors.joining(", "),
                            msg -> msg.isEmpty() ? "보내는 파라미터를 다시 확인해주세요." : msg));
        }

        if (e instanceof HandlerMethodValidationException exception) {
            return exception.getParameterValidationResults().stream()  // getValidationResults() 사용
                    .map(ParameterValidationResult::getResolvableErrors)
                    .flatMap(Collection::stream)
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.collectingAndThen(Collectors.joining(", "),
                            msg -> msg.isEmpty() ? "보내는 파라미터를 다시 확인해주세요." : msg));
        }

        if (e instanceof MethodArgumentTypeMismatchException exception) {

            String parameterName = exception.getName();  // 요청 파라미터 이름
            String value = (String) exception.getValue(); // 전달된 값

            return String.format("요청 파라미터 '%s'의 값 '%s'은(는) 유효하지 않습니다.", parameterName, value);
        }

        return e.getMessage();
    }
}
