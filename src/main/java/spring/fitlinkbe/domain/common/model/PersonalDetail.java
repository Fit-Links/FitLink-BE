package spring.fitlinkbe.domain.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetail {

    private Long personalDetailId;

    private String name;

    private Trainer trainer;

    private Member member;

    private Long trainerId;

    private Long memberId;

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

    public void registerMember(String name, LocalDate birthDate, PhoneNumber phoneNumber,
                               String profileUrl, Gender gender, Member member) {
        register(name, birthDate, phoneNumber, profileUrl, gender);
        this.member = member;
    }

    public void registerTrainer(String name, LocalDate birthDate, PhoneNumber phoneNumber,
                                String profileUrl, Gender gender, Trainer trainer) {
        register(name, birthDate, phoneNumber, profileUrl, gender);
        this.trainer = trainer;
    }

    private void register(String name, LocalDate birthDate, PhoneNumber phoneNumber, String profileUrl, Gender gender) {
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.profilePictureUrl = profileUrl;
        this.gender = gender;
        this.status = Status.NORMAL;
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
