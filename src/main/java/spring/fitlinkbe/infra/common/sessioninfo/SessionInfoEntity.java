package spring.fitlinkbe.infra.common.sessioninfo;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

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

    private Long trainerId;

    private Long memberId;

    private int totalCount;

    private int remainCount;

    public static SessionInfoEntity from(SessionInfo sessionInfo) {

        return SessionInfoEntity.builder()
                .sessionInfoId(sessionInfo.getSessionInfoId() != null ? sessionInfo.getSessionInfoId() : null)
                .trainerId(sessionInfo.getTrainerId())
                .memberId(sessionInfo.getMemberId())
                .totalCount(sessionInfo.getTotalCount())
                .remainCount(sessionInfo.getRemainCount())
                .build();
    }

    public SessionInfo toDomain() {
        return SessionInfo.builder()
                .SessionInfoId(sessionInfoId)
                .trainerId(trainerId)
                .memberId(memberId)
                .totalCount(totalCount)
                .remainCount(remainCount)
                .build();
    }
}
