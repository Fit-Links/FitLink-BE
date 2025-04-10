package spring.fitlinkbe.domain.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDate;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.common.enums.UserRole.TRAINER;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetail {

    private Long personalDetailId;

    private String name;

    private Long memberId;

    private Long trainerId;

    private String profilePictureUrl;

    private Gender gender;

    private LocalDate birthDate;

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

    public void registerMember(String name, LocalDate birthDate,
                               String profileUrl, Gender gender, Member member) {
        register(name, birthDate, profileUrl, gender);
        this.memberId = member.getMemberId();
    }

    public void registerTrainer(String name, LocalDate birthDate,
                                String profileUrl, Gender gender, Trainer trainer) {
        register(name, birthDate, profileUrl, gender);
        this.trainerId = trainer.getTrainerId();
    }

    private void register(String name, LocalDate birthDate, String profileUrl, Gender gender) {
        this.name = name;
        this.birthDate = birthDate;
        this.profilePictureUrl = profileUrl;
        this.gender = gender;
        this.status = Status.NORMAL;
    }

    public void update(String name, String phoneNumber) {
        if (name != null) {
            this.name = name;
        }
        if (phoneNumber != null) {
            this.phoneNumber = new PhoneNumber(phoneNumber);
        }
    }

    public UserRole getUserRole() {
        return trainerId == null ? MEMBER : TRAINER;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = new PhoneNumber(phoneNumber);
    }

    public void verifySnsEmail(PhoneNumber phoneNumber) {
        if (this.status != Status.REQUIRED_SMS) {
            throw new CustomException(ErrorCode.USER_STATUS_NOT_ALLOWED);
        }
        this.phoneNumber = phoneNumber;
        this.status = Status.REQUIRED_REGISTER;
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
        REQUIRED_REGISTER, // 회원 또는 트레이너 등록 필요 상태
        SLEEP, // 잠시 중지된 상태
        SUSPEND, // 중지된 상태
        DELETE // 삭제된 상태
    }

}
