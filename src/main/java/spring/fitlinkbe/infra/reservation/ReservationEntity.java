package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedDate;
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

    @CreatedDate
    private LocalDateTime createdAt;

    public static ReservationEntity from(Reservation reservation, EntityManager em) {

        return ReservationEntity.builder()
                .reservationId(reservation.getReservationId() != null
                        ? reservation.getReservationId() : null)
                .trainer(reservation.getTrainer() != null
                        ? em.getReference(TrainerEntity.class, reservation.getTrainer().getTrainerId()) : null)
                .member(reservation.isReservationNotAllowed() ? null
                        : em.getReference(MemberEntity.class, reservation.getMember().getMemberId()))
                .sessionInfo((reservation.isReservationNotAllowed() || reservation.getSessionInfo() == null) ? null
                        : em.getReference(SessionInfoEntity.class, reservation.getSessionInfo().getSessionInfoId()))
                .name(reservation.getName())
                .reservationDates(reservation.getReservationDates())
                .changeDate(reservation.getChangeDate())
                .dayOfWeek(reservation.getDayOfWeek())
                .status(reservation.getStatus())
                .cancelReason(reservation.getCancelReason())
                .isDayOff(reservation.isDayOff())
                .createdAt(reservation.getCreatedAt())
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
