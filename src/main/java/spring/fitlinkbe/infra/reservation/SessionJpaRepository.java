package spring.fitlinkbe.infra.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, Long> {

    Optional<SessionEntity> findByReservation_ReservationId(Long reservationId);
}
