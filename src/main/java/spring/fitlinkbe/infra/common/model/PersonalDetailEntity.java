package spring.fitlinkbe.infra.common.model;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PhoneNumber;

import static spring.fitlinkbe.domain.common.model.PersonalDetail.*;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "personal_detail")
public class PersonalDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long personalDetailId;

    private String name;

    private Long trainerId;

    private Long memberId;

    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String birthDate;

    private String phoneNumber;

    private String email;

    @Enumerated(EnumType.STRING)
    private OauthProvider oauthProvider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private Status status;

    public PersonalDetail toDomain() {
        return PersonalDetail.builder()
                .personalDetailId(personalDetailId)
                .name(name)
                .trainerId(trainerId)
                .profilePictureUrl(profilePictureUrl)
                .gender(gender)
                .birthDate(birthDate)
                .phoneNumber(new PhoneNumber(phoneNumber))
                .email(email)
                .oauthProvider(oauthProvider)
                .providerId(providerId)
                .status(status)
                .build();
    }

    public static PersonalDetailEntity from(PersonalDetail personalDetail) {
        return PersonalDetailEntity.builder()
                .personalDetailId(personalDetail.getPersonalDetailId())
                .name(personalDetail.getName())
                .trainerId(personalDetail.getTrainerId())
                .memberId(personalDetail.getMemberId())
                .profilePictureUrl(personalDetail.getProfilePictureUrl())
                .gender(personalDetail.getGender())
                .birthDate(personalDetail.getBirthDate())
                .phoneNumber(personalDetail.getPhoneNumber())
                .email(personalDetail.getEmail())
                .oauthProvider(personalDetail.getOauthProvider())
                .providerId(personalDetail.getProviderId())
                .status(personalDetail.getStatus())
                .build();
    }
}
