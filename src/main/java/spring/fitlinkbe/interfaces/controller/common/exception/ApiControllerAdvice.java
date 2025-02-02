package spring.fitlinkbe.interfaces.controller.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;


@RestControllerAdvice
@Slf4j(topic = "ExceptionLogger")
public class ApiControllerAdvice {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(CustomException.class)
    public ApiResultResponse<Object> handlerCustomException(CustomException e) {
        log.info("CustomException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.OK, false, e.getMessage(), null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResultResponse<Object> handlerException(Exception e) {
        e.printStackTrace();
        log.error("Exception is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, false, null);
    }
}
