package spring.fitlinkbe.domain.common.model;

import lombok.Getter;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;

@Getter
public class PhoneNumber {
    private final String phoneNumber;

    public PhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;

        if (!isValidPhoneNumber(phoneNumber)) {
            throw new CustomException(ErrorCode.INVALID_PHONE_NUMBER_FORMAT);
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return true;
        }

        // 문자열 처리
        if (phoneNumber.isEmpty()) {
            return false;
        }

        // 정규식으로 숫자만 포함되며 10자리 또는 11자리인지 확인
        return phoneNumber.matches("^\\d{10,11}$");
    }
}
