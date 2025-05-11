package spring.fitlinkbe.infra.trainer;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.PhoneNumber;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // proxy 객체 생성을 위해
@AllArgsConstructor // 빌더 패턴 사용을 위해
@Table(name = "trainer")
public class TrainerEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainerId;

    private String trainerCode;

    private String name;
    
    private String phoneNumber;

    private String profilePictureUrl;

    public static TrainerEntity from(Trainer trainer) {
        return TrainerEntity.builder()
                .trainerId(trainer.getTrainerId() != null ? trainer.getTrainerId() : null)
                .trainerCode(trainer.getTrainerCode())
                .name(trainer.getName())
                .phoneNumber(trainer.getPhoneNumber())
                .profilePictureUrl(trainer.getProfilePictureUrl())
                .build();
    }

    public Trainer toDomain() {
        return Trainer.builder()
                .trainerId(trainerId)
                .trainerCode(trainerCode)
                .phoneNumber(new PhoneNumber(phoneNumber))
                .profilePictureUrl(profilePictureUrl)
                .name(name)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

}