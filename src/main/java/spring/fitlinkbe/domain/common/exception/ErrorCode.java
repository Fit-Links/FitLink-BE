package spring.fitlinkbe.domain.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // Trainer 관련 ErrorCode
    TRAINER_IS_NOT_FOUND("트레이너 정보가 존재하지 않습니다.", 404),

    // Member 관련 ErrorCode

    // Reservation 관련 ErrorCode
    RESERVATION_IS_FAILED("예약에 실패하였습니다.", 400),

    // Session 관련 ErrorCode

    // Notification 관련 ErrorCode

    // Auth 관련 ErrorCode
    UNSUPPORTED_OAUTH_PROVIDER("지원하지 않는 OAuth 제공자입니다.", 400),
    EXPIRED_TOKEN("토큰이 만료되었습니다.", 401),
    USER_STATUS_NOT_ALLOWED("해당 유저 상태에서는 요청 불가합니다.", 403),

    // Common ErrorCode
    PERSONAL_DETAIL_NOT_FOUND("Personal Detail 정보가 존재하지 않습니다.", 404),
    ;

    private final String msg;
    private final int status;
}
