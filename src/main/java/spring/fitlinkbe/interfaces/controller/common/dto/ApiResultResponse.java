package spring.fitlinkbe.interfaces.controller.common.dto;


import org.springframework.http.HttpStatus;

public record ApiResultResponse<T>(int status, boolean success, String msg, T data) {

    public static <T> ApiResultResponse<T> of(HttpStatus httpStatus, boolean success, String message, T data) {
        return new ApiResultResponse<>(httpStatus.value(), success, message, data);
    }

    public static <T> ApiResultResponse<T> of(HttpStatus httpStatus, boolean success, T data) {
        return new ApiResultResponse<>(httpStatus.value(), success, httpStatus.name(), data);
    }

    public static <T> ApiResultResponse<T> ok(T data) {
        return of(HttpStatus.OK, true, data);
    }

}
