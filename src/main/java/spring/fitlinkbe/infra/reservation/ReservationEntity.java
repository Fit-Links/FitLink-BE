package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;
import spring.fitlinkbe.infra.common.sessioninfo.SessionInfoEntity;
import spring.fitlinkbe.infra.member.MemberEntity;
import spring.fitlinkbe.infra.trainer.TrainerEntity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(columnDefinition = "JSON")
    @Convert(converter = LocalDateTimeListConverter.class)
    private List<LocalDateTime> reservationDates;

    private LocalDateTime changeDate;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    private Reservation.Status status;

    private String cancelReason;

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
                .reservationDates(reservation.getReservationDates())
                .changeDate(reservation.getChangeDate())
                .dayOfWeek(reservation.getDayOfWeek())
                .status(reservation.getStatus())
                .cancelReason(reservation.getCancelReason())
                .isDayOff(reservation.isDayOff())
                .build();
    }

    public Reservation toDomain() {
        return Reservation.builder()
                .reservationId(reservationId)
                .member(isReservationNotAllowed() ? null : Hibernate.isInitialized(member) ? member.toDomain() : null)
                .trainer(Hibernate.isInitialized(trainer) ? trainer.toDomain() : null)
                .sessionInfo((sessionInfo == null || isReservationNotAllowed()) ? null :
                        Hibernate.isInitialized(sessionInfo) ? sessionInfo.toDomain() : null)
                .name(name)
                .reservationDates(reservationDates)
                .changeDate(changeDate)
                .dayOfWeek(dayOfWeek)
                .status(status)
                .cancelReason(cancelReason)
                .isDayOff(isDayOff)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }

    private boolean isReservationNotAllowed() {
        return (isDayOff || (status == Reservation.Status.DISABLED_TIME_RESERVATION));
    }
}
