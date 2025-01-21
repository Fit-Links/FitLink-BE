package spring.fitlinkbe.domain.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

    // Trainer 관련 ErrorCode
    TRAINER_IS_NOT_FOUND("트레이너 정보가 존재하지 않습니다."),

    // Member 관련 ErrorCode

    // Reservation 관련 ErrorCode
    RESERVATION_IS_FAILED("예약에 실패하였습니다.");

    // Session 관련 ErrorCode

    // Notification 관련 ErrorCode

    private final String msg;
}
