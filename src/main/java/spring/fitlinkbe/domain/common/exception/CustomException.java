package spring.fitlinkbe.domain.common.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String msg;

    public CustomException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.msg = errorCode.getMsg();
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, msg);
    }
}
