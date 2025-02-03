package spring.fitlinkbe.infra.member;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.PhoneNumber;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.trainer.TrainerEntity;

import java.time.LocalDate;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class MemberEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    private String name;

    private LocalDate birthDate;

    private String phoneNumber;

    private Boolean isRequest;

    private Boolean isConnected;

    public static MemberEntity from(Member member) {

        return MemberEntity.builder()
                .memberId(member.getMemberId() != null ? member.getMemberId() : null)
                .trainer(TrainerEntity.from(member.getTrainer()))
                .name(member.getName())
                .birthDate(member.getBirthDate())
                .isRequest(member.getIsRequest())
                .isConnected(member.getIsConnected())
                .build();
    }

    public Member toDomain() {
        return Member.builder()
                .memberId(memberId)
                .trainer(trainer.toDomain())
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
