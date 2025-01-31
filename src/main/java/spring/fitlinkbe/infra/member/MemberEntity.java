package spring.fitlinkbe.infra.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import spring.fitlinkbe.domain.common.model.PhoneNumber;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.trainer.TrainerEntity;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class MemberEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    private String name;

    private LocalDate birthDate;

    private String phoneNumber;

    private Boolean isRequest;

    private Boolean isConnected;

    public Member toDomain() {
        return Member.builder()
                .id(id)
                .trainerId(trainer.getTrainerId())
                .name(name)
                .birthDate(birthDate)
                .phoneNumber(new PhoneNumber(phoneNumber))
                .isRequest(isRequest)
                .isConnected(isConnected)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
