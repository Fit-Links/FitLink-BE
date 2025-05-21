package spring.fitlinkbe.interfaces.controller.common.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;

import javax.security.sasl.AuthenticationException;
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

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(AuthenticationException.class)
    public ApiResultResponse<Object> handlerAuthenticationException(AuthenticationException e) {
        log.error("AuthenticationException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.UNAUTHORIZED, false, e.getMessage(), null);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResultResponse<Object> handlerAccessDeniedException(AccessDeniedException e) {
        log.error("AccessDeniedException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.FORBIDDEN, false, e.getMessage(), null);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResultResponse<Object> handlerMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.BAD_REQUEST, false, getErrorCustomMessage(e), null);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ApiResultResponse<Object> handlerMethodValidationException(HandlerMethodValidationException e) {
        log.error("HandlerMethodValidationException is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.BAD_REQUEST, false, getErrorCustomMessage(e), null);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({
            ExpiredJwtException.class,
            UnsupportedJwtException.class,
            MalformedJwtException.class,
            SignatureException.class,
            JwtException.class,
    })
    public ApiResultResponse<Object> handleJwtException(Exception e) {
        log.warn("JWT authentication error: {} \nerror class: {}", e.getMessage(), e.getClass());
        return ApiResultResponse.of(
                HttpStatus.UNAUTHORIZED,
                false,
                "유효하지 않은 토큰입니다. 다시 로그인해주세요.",
                null
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResultResponse<Object> handlerException(Exception e) {
        log.error("Exception is occurred! {}", e.getMessage());
        return ApiResultResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, false, e.getMessage(), null);
    }
}
