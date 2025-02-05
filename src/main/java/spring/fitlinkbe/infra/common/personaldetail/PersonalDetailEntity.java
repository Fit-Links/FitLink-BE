package spring.fitlinkbe.infra.common.personaldetail;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.common.model.PhoneNumber;
import spring.fitlinkbe.infra.member.MemberEntity;
import spring.fitlinkbe.infra.trainer.TrainerEntity;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private MemberEntity member;

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
                .trainerId(trainer != null ? trainer.getTrainerId() : null)
                .memberId(member != null ? member.getMemberId() : null)
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
                .personalDetailId(personalDetail.getPersonalDetailId() != null ? personalDetail.getPersonalDetailId() : null)
                .name(personalDetail.getName())
                .trainer(personalDetail.getTrainer() != null ? TrainerEntity.from(personalDetail.getTrainer()) : null)
                .member(personalDetail.getMember() != null ? MemberEntity.from(personalDetail.getMember())
                        : null)
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
