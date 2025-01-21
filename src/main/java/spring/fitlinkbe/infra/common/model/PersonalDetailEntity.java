package spring.fitlinkbe.infra.common.model;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.PersonalDetail;

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

    private Gender gender;

    private String birthDate;

    private String phoneNumber;

    private String email;

    private OauthProvider oauth_provider;

    private Status status;

    public PersonalDetail toDomain() {
        return PersonalDetail.builder()
                .personalDetailId(personalDetailId)
                .name(name)
                .trainerId(trainerId)
                .profilePictureUrl(profilePictureUrl)
                .gender(gender)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .email(email)
                .oauth_provider(oauth_provider)
                .status(status)
                .build();
    }
}
