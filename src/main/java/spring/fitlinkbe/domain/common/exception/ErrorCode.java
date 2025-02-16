package spring.fitlinkbe.domain.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // Trainer 관련 ErrorCode
    TRAINER_IS_NOT_FOUND("트레이너 정보가 존재하지 않습니다.", 404),

    // Member 관련 ErrorCode
    MEMBER_DETAIL_NOT_FOUND("멤버 상세 정보를 찾지 못하였습니다", 404),


    // Reservation 관련 ErrorCode
    RESERVATION_IS_FAILED("예약에 실패하였습니다.", 400),
    SET_DISABLE_DATE_FAILED("예약 불가 설정에 실패하였습니다.", 400),
    RESERVATION_IS_ALREADY_CANCEL("이미 예약이 취소되었습니다.", 400),
    RESERVATION_CANCEL_NOT_ALLOWED("예약 취소를 할 수 없는 상태입니다.", 400),
    RESERVATION_NOT_FOUND("예약 정보를 찾지 못하였습니다.", 404),

    // Session 관련 ErrorCode
    SESSION_NOT_FOUND("세션 정보를 찾지 못하였습니다.", 404),
    SESSION_IS_ALREADY_CANCEL("이미 세션이 취소되었습니다.", 400),
    SESSION_IS_ALREADY_COMPLETED("이미 끝난 세션은 취소할 수 없습니다.", 400),

    // Notification 관련 ErrorCode

    // Auth 관련 ErrorCode
    UNSUPPORTED_OAUTH_PROVIDER("지원하지 않는 OAuth 제공자입니다.", 400),
    EXPIRED_TOKEN("토큰이 만료되었습니다.", 401),
    AUTH_FAILED("인증에 실패하였습니다.", 401),
    USER_STATUS_NOT_ALLOWED("해당 유저 상태에서는 요청 불가합니다.", 403),
    NEED_REQUIRED_SMS_STATUS("유저의 상태가 소셜 로그인만 진행된 상태어야 합니다", 403),

    TOKEN_NOT_FOUND("토큰 정보를 찾지 못하였습니다.", 404),


    // Common ErrorCode
    INVALID_PHONE_NUMBER_FORMAT("유효하지 않은 전화번호 형식입니다.", 400),
    PERSONAL_DETAIL_NOT_FOUND("Personal Detail 정보가 존재하지 않습니다.", 404),
    ;

    private final String msg;
    private final int status;
}
