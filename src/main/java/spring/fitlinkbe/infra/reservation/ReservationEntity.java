package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.common.sessioninfo.SessionInfoEntity;
import spring.fitlinkbe.infra.member.MemberEntity;
import spring.fitlinkbe.infra.trainer.TrainerEntity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // proxy 객체 생성을 위해
@AllArgsConstructor // 빌더 패턴 사용을 위해
@Table(name = "reservation")
public class ReservationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private TrainerEntity trainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_info_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private SessionInfoEntity sessionInfo;

    private String name;

    private LocalDateTime reservationDate;

    private LocalDateTime changeDate;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    private Reservation.Status status;

    private String cancelReason;

    private int priority;

    private boolean isApproved;

    private boolean isFixed;

    private boolean isDisabled;

    private boolean isDayOff;

    public static ReservationEntity from(Reservation reservation) {

        return ReservationEntity.builder()
                .reservationId(reservation.getReservationId() != null
                        ? reservation.getReservationId() : null)
                .trainer(TrainerEntity.from(reservation.getTrainer()))
                .member(reservation.isReservationNotAllowed() ? null
                        : MemberEntity.from(reservation.getMember()))
                .sessionInfo(reservation.isReservationNotAllowed() ? null
                        : SessionInfoEntity.from(reservation.getSessionInfo()))
                .name(reservation.getName())
                .reservationDate(reservation.getReservationDate())
                .changeDate(reservation.getChangeDate())
                .dayOfWeek(reservation.getDayOfWeek())
                .status(reservation.getStatus())
                .cancelReason(reservation.getCancelReason())
                .isApproved(reservation.isApproved())
                .priority(reservation.getPriority())
                .isFixed(reservation.isFixed())
                .isDayOff(reservation.isDayOff())
                .isDisabled(reservation.isDisabled())
                .build();
    }

    public Reservation toDomain() {
        return Reservation.builder()
                .reservationId(reservationId)
                .member((isDayOff || isDisabled) ? null : member.toDomain())
                .trainer(trainer.toDomain())
                .sessionInfo((isDayOff || isDisabled) ? null : sessionInfo.toDomain())
                .name(name)
                .reservationDate(reservationDate)
                .changeDate(changeDate)
                .dayOfWeek(dayOfWeek)
                .status(status)
                .cancelReason(cancelReason)
                .priority(priority)
                .isApproved(isApproved)
                .isFixed(isFixed)
                .isDayOff(isDayOff)
                .isDisabled(isDisabled)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
