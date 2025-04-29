package spring.fitlinkbe.domain.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // Trainer 관련 ErrorCode
    TRAINER_IS_NOT_FOUND("트레이너 정보가 존재하지 않습니다.", 404),
    ALREADY_APPLIED_AVAILABLE_TIMES("이미 적용된 수업 시간이 있습니다.", 409),
    ALREADY_SCHEDULED_AVAILABLE_TIMES("이미 적용 대기중인 스케줄이 있습니다.", 409),

    AVAILABLE_TIMES_IS_NOT_FOUND("해당 날짜에 수업 시간이 없습니다.", 404),
    DAY_OFF_DUPLICATED("해당 날짜에 이미 적용된 휴무일이 있습니다.", 409),
    DAY_OFF_NOT_FOUND("휴무일이 존재하지 않습니다.", 404),
    CONFIRMED_RESERVATION_EXISTS("해당 날짜에 확정된 예약이 존재합니다.", 409),

    // Member 관련 ErrorCode
    MEMBER_DETAIL_NOT_FOUND("멤버 상세 정보를 찾지 못하였습니다", 404),
    MEMBER_NOT_FOUND("멤버 정보를 찾지 못하였습니다.", 404),
    MEMBER_NOT_CONNECTED_TRAINER("연결된 트레이너가 존재하지 않습니다.", 400),
    DISCONNECT_AVAILABLE_AFTER_ACCEPTED("트래이너 연동 요청 이후에만 연결을 해제할 수 있습니다.", 409),

    CONNECT_AVAILABLE_AFTER_DISCONNECTED("이미 연결 요청중 또는 연결된 트레이너가 존재합니다.", 409),

    WORKOUT_SCHEDULE_NOT_FOUND("운동 희망일이 존재하지 않습니다", 404),
    DUPLICATED_WORKOUT_SCHEDULE("운동 희망일이 겹칩니다", 400),
    MEMBER_PERMISSION_DENIED("회원은 권한이 없습니다.", 403),

    // Reservation 관련 ErrorCode

    RESERVATION_IS_FAILED("예약에 실패하였습니다.", 400),

    APPROVED_RESERVATION_EXISTS("승인된 예약이 존재합니다.", 409),

    RESERVATION_NOT_ALLOWED("예약을 할 수 있는 상태가 아닙니다.", 400),
    SET_DISABLE_DATE_FAILED("예약 불가 설정에 실패하였습니다.", 400),
    RESERVATION_CANCEL_FAILED("예약 취소에 실패하였습니다.", 400),
    RESERVATION_IS_ALREADY_CANCEL("이미 예약이 취소되었습니다.", 400),
    RESERVATION_IS_NOT_WAITING_STATUS("예약 상태가 대기 상태가 아닙니다.", 400),
    RESERVATION_WAITING_MEMBERS_EMPTY("이 날짜에 예약 대기자가 없습니다.", 400),
    RESERVATION_COMPLETE_NOT_ALLOWED("다른 사람의 예약을 완료시킬 수 없습니다.", 400),
    RESERVATION_IS_ALREADY_COMPLETED("이미 예약이 완료되었습니다.", 400),
    RESERVATION_CHANGE_REQUEST_NOT_ALLOWED("예약 변경을 요청할 수 없는 상태입니다.", 400),
    RESERVATION_RELEASE_NOT_ALLOWED("예약 해지를 할 수 없는 상태입니다.", 400),
    RESERVATION_CANCEL_NOT_ALLOWED("예약 취소를 할 수 없는 상태입니다.", 400),
    RESERVATION_REFUSE_NOT_ALLOWED("예약 거절을 할 수 없는 상태입니다.", 400),
    RESERVATION_APPROVE_NOT_ALLOWED("예약 확정을 할 수 없는 상태입니다.", 400),
    RESERVATION_IS_ALREADY_APPROVED("이미 예약이 승인 되었습니다.", 400),
    RESERVATION_NOT_FOUND("예약 정보를 찾지 못하였습니다.", 404),
    RESERVATION_DATE_NOT_FOUND("예약 날짜를 찾지 못하였습니다.", 404),
    FAILED_TO_CONVERT_JSON("Failed to convert LocalDateTime list to JSON", 400),
    FAILED_TO_CONVERT_LIST("Failed to convert LocalDateTime list to List", 400),

    // Session 관련 ErrorCode
    SESSION_CREATE_FAILED("세션 생성에 실패하였습니다.", 400),
    SESSION_NOT_FOUND("세션 정보를 찾지 못하였습니다.", 404),
    SESSION_IS_ALREADY_CANCEL("이미 세션이 취소되었습니다.", 400),
    SESSION_IS_ALREADY_END("이미 세션이 종료되었습니다.", 400),
    SESSION_REMAINING_COUNT_NOT_VALID("세션 남은 횟수가 적절하지 않습니다.", 400),

    // Notification 관련 ErrorCode
    NOTIFICATION_NOT_FOUND("알림 정보를 찾지 못하였습니다.", 404),
    NOTIFICATION_STRANGE_TYPE("알 수 없는 알림 타입입니다", 400),

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
    CONNECTING_INFO_NOT_FOUND("연결 정보가 없습니다.", 404),

    INVALID_PARAMETER("유효하지 않은 파라미터입니다.", 400),
    PHONE_NUMBER_NOT_FOUND("전화번호를 찾을 수 없습니다.", 400),

    INVALID_CONTENT_TYPE("유효하지 않은 Content-Type 입니다.", 400),
    INVALID_CONTENT_LENGTH("유효하지 않은 Content-Length 입니다.", 400),

    // Attachment 관련 ErrorCode
    ATTACHMENT_NOT_FOUND("첨부파일을 찾을 수 없습니다.", 404),

    // Outbox 관련
    OUTBOX_IS_FAILED("outbox 데이터 생성에 실패하였습니다.", 400),
    OUTBOX_IS_NOT_FOUND("outbox 데이터가 존재하지 않습니다.", 404),
    OUTBOX_IS_ALREADY_DONE("이미 완료된 outbox 데이터 입니다.", 400),
    OUTBOX_IS_ALREADY_FAIL("이미 실패한 outbox 데이터 입니다.", 400),
    OUTBOX_IS_NOT_INIT_STATUS("outbox 데이터가 초기 상태가 아닙니다.", 400),
    OUTBOX_PAYLOAD_INVALID("outbox 데이터가 정상 상태가 아닙니다.", 400),

    ;

    private final String msg;
    private final int status;
}
