package spring.fitlinkbe.domain.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetail {

    private Long personalDetailId;

    private String name;

    private Long trainerId;

    private Long memberId;

    private String profilePictureUrl;

    private Gender gender;

    private String birthDate;

    private PhoneNumber phoneNumber;

    private String email;

    private OauthProvider oauthProvider;

    private String providerId;

    private Status status;


    public String getPhoneNumber() {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.getPhoneNumber();
    }

    public enum Gender {
        MALE, FEMALE
    }

    public enum OauthProvider {
        GOOGLE, KAKAO, NAVER, APPLE
    }

    public enum Status {
        NORMAL, // 기본 상태
        REQUIRED_SMS, // SMS 연동이 필요한 상태
        SLEEP, // 잠시 중지된 상태
        SUSPEND, // 중지된 상태
        DELETE // 삭제된 상태
    }

}
