package spring.fitlinkbe.infra.trainer;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.common.model.PersonalDetailEntity;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_detail_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)) //논리적으로만 관계를 맺기 위해
    private PersonalDetailEntity personalDetail;

    private String trainerCode;

    public static TrainerEntity from(Trainer trainer) {
        return TrainerEntity.builder().build();
    }

    public Trainer toDomain() {
        return Trainer.builder()
                .trainerId(trainerId)
                .personalDetail(personalDetail.toDomain())
                .trainerCode(trainerCode)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

}