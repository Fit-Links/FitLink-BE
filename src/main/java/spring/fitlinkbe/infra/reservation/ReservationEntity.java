package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

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

    private Long memberId;

    private Long trainerId;

    private Long sessionInfoId;

    private String name;

    private LocalDateTime reservationDate;

    private LocalDateTime changeDate;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    private Reservation.Status status;

    private String cancelReason;

    private boolean approvedCancel;

    private int priority;

    private boolean isFixed;

    private boolean isDisabled;

    private boolean isDayOff;

    public static ReservationEntity from(Reservation reservation) {

        return ReservationEntity.builder()
                .reservationId(reservation.getReservationId() != null ? reservation.getReservationId() : null)
                .trainerId(reservation.getTrainerId())
                .memberId(reservation.getMemberId())
                .sessionInfoId(reservation.getSessionInfoId())
                .name(reservation.getName())
                .reservationDate(reservation.getReservationDate())
                .changeDate(reservation.getChangeDate())
                .dayOfWeek(reservation.getDayOfWeek())
                .status(reservation.getStatus())
                .cancelReason(reservation.getCancelReason())
                .approvedCancel(reservation.isApprovedCancel())
                .priority(reservation.getPriority())
                .isFixed(reservation.isFixed())
                .isDayOff(reservation.isDayOff())
                .isDisabled(reservation.isDisabled())
                .build();
    }

    public Reservation toDomain() {
        return Reservation.builder()
                .reservationId(reservationId)
                .memberId(memberId)
                .name(name)
                .sessionInfoId(sessionInfoId)
                .reservationDate(reservationDate)
                .changeDate(changeDate)
                .dayOfWeek(dayOfWeek)
                .status(status)
                .cancelReason(cancelReason)
                .approvedCancel(approvedCancel)
                .priority(priority)
                .isFixed(isFixed)
                .isDayOff(isDayOff)
                .isDisabled(isDisabled)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
