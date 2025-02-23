package spring.fitlinkbe.interfaces.controller.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;

import java.net.BindException;

import static spring.fitlinkbe.support.utils.MessageConvertUtils.getErrorCustomMessage;

@RestControllerAdvice
@Slf4j(topic = "ExceptionLogger")
public class ApiControllerAdvice {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(CustomException.class)
    public ApiResultResponse<Object> handlerCustomException(CustomException e) {
        log.error("CustomException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.valueOf(e.getStatus()), false, e.getMessage(), null);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BindException.class)
    public ApiResultResponse<Object> handlerBindingException(CustomException e) {
        log.error("BindException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.valueOf(e.getStatus()), false, e.getMessage(), null);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResultResponse<Object> handlerHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.BAD_REQUEST, false, e.getMessage(), null);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResultResponse<Object> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.BAD_REQUEST, false, getErrorCustomMessage(e),
                null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResultResponse<Object> handlerException(Exception e) {
        log.error("Exception is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, false, e.getMessage(), null);
    }
}
