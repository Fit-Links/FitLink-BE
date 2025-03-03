package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.*;
import lombok.*;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.infra.common.model.BaseTimeEntity;

import static spring.fitlinkbe.domain.reservation.Session.Status;

@Entity
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // proxy 객체 생성을 위해
@AllArgsConstructor // 빌더 패턴 사용을 위해
@Table(name = "session")
public class SessionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ReservationEntity reservation;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String cancelReason;

    private boolean isCompleted;

    public static SessionEntity from(Session session, EntityManager em) {

        return SessionEntity.builder()
                .sessionId(session.getSessionId() != null
                        ? session.getSessionId() : null)
                .reservation(em.getReference(ReservationEntity.class, session.getReservation().getReservationId()))
                .status(session.getStatus())
                .cancelReason(session.getCancelReason())
                .isCompleted(session.isCompleted())
                .build();
    }

    public Session toDomain() {
        return Session.builder()
                .sessionId(sessionId)
                .reservation(reservation.toDomain())
                .status(status)
                .cancelReason(cancelReason)
                .isCompleted(isCompleted)
                .build();
    }
}
