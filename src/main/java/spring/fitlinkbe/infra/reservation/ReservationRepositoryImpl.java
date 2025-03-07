package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.reservation.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;
    private final SessionJpaRepository sessionJpaRepository;
    private final EntityManager em;

    @Override
    public List<Reservation> getPlainReservations() {

        return reservationJpaRepository.findAll()
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getReservations() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);

        return reservationJpaRepository.findAllAfterToday(now)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getFixedReservations() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(2);

        return reservationJpaRepository.findFixedStatus(FIXED_RESERVATION, now)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getReservationsWithWaitingStatus(Reservation.Status status, Long trainerId) {
        LocalDateTime now = LocalDateTime.now().minusSeconds(2);

        return reservationJpaRepository.findWaitingStatus(status, trainerId, now)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getReservations(UserRole role, Long userId) {
        LocalDateTime now = LocalDateTime.now().minusSeconds(2);

        if (role == MEMBER) { //멤버의 경우
            return reservationJpaRepository.findByMember_MemberId(userId, now)
                    .stream()
                    .map(ReservationEntity::toDomain)
                    .toList();
        }

        return reservationJpaRepository.findByTrainer_TrainerId(userId, now)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> saveReservations(List<Reservation> canceledReservations) {
        List<ReservationEntity> entities = canceledReservations.stream()
                .map(r -> ReservationEntity.from(r, em))
                .toList();

        List<ReservationEntity> savedEntities = reservationJpaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<Reservation> getReservation(Long reservationId) {
        Optional<ReservationEntity> findEntity = reservationJpaRepository.findByIdJoinFetch(reservationId);
        if (findEntity.isPresent()) {
            return findEntity.map(ReservationEntity::toDomain);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Reservation> reserveSession(Reservation reservation) {
        ReservationEntity reservationEntity = reservationJpaRepository.save(ReservationEntity.from(reservation, em));

        return Optional.of(reservationEntity.toDomain());
    }

    @Override
    public Optional<Session> getSession(Long reservationId) {
        Optional<SessionEntity> findEntity = sessionJpaRepository.findByReservation_ReservationId(reservationId);
        if (findEntity.isPresent()) {
            return findEntity.map(SessionEntity::toDomain);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Session> createSession(Session session) {
        SessionEntity savedEntity = sessionJpaRepository.save(SessionEntity.from(session, em));

        return Optional.of(savedEntity.toDomain());
    }

    @Override
    public List<Session> saveSessions(List<Session> sessions) {
        List<SessionEntity> entities = sessions.stream()
                .map(session -> SessionEntity.from(session, em))
                .toList();

        List<SessionEntity> savedEntities = sessionJpaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(SessionEntity::toDomain)
                .toList();
    }
}
