package spring.fitlinkbe.infra.common.sessioninfo;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.member.MemberEntity;
import spring.fitlinkbe.infra.trainer.TrainerEntity;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "session_info", uniqueConstraints = {@UniqueConstraint(name = "UNIQUE_SESSION_INFO",
        columnNames = {"trainer_id", "member_id"})})
public class SessionInfoEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionInfoId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private MemberEntity member;

    private int totalCount;

    private int remainCount;

    public static SessionInfoEntity from(SessionInfo sessionInfo) {

        return SessionInfoEntity.builder()
                .sessionInfoId(sessionInfo.getSessionInfoId() != null ? sessionInfo.getSessionInfoId() : null)
                .trainer(TrainerEntity.from(sessionInfo.getTrainer()))
                .member(MemberEntity.from(sessionInfo.getMember()))
                .totalCount(sessionInfo.getTotalCount())
                .remainCount(sessionInfo.getRemainCount())
                .build();
    }

    public SessionInfo toDomain() {
        return SessionInfo.builder()
                .SessionInfoId(sessionInfoId)
                .trainer(trainer.toDomain())
                .member(member.toDomain())
                .totalCount(totalCount)
                .remainCount(remainCount)
                .build();
    }
}
