package spring.fitlinkbe.infra.common.connectingInfo;


import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.common.model.ConnectingInfo;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.member.MemberEntity;
import spring.fitlinkbe.infra.trainer.TrainerEntity;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "connecting_info")
public class ConnectingInfoEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long connectingInfoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private MemberEntity member;

    @Enumerated(EnumType.STRING)
    private ConnectingInfo.ConnectingStatus status;

    public static ConnectingInfoEntity from(ConnectingInfo connectingInfo) {
        return ConnectingInfoEntity.builder()
                .connectingInfoId(connectingInfo.getConnectingInfoId())
                .trainer(TrainerEntity.from(connectingInfo.getTrainer()))
                .member(MemberEntity.from(connectingInfo.getMember()))
                .status(connectingInfo.getStatus())
                .build();
    }

    public ConnectingInfo toDomain() {
        return ConnectingInfo.builder()
                .connectingInfoId(connectingInfoId)
                .trainer(trainer.toDomain())
                .member(member.toDomain())
                .status(status)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
