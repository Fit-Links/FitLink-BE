package spring.fitlinkbe.domain.common.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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

    public int getStatus() {
        return errorCode.getStatus();
    }
}
