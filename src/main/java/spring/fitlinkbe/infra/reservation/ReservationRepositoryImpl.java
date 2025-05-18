package spring.fitlinkbe.infra.reservation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.reservation.Session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;
    private final SessionJpaRepository sessionJpaRepository;
    private final EntityManager em;

    @Override
    public List<Reservation> getReservations() {

        return reservationJpaRepository.findAllJoinFetch()
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getFixedReservations() {

        return reservationJpaRepository.findFixedStatus()
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getFixedReservations(Long memberId) {

        return reservationJpaRepository.findFixedStatus(memberId)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getFixedReservations(Long trainerId, LocalDateTime fixedReservationDate) {

        return reservationJpaRepository.findAllFixedReservation(trainerId, fixedReservationDate)

                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getReservationsWithWaitingStatus(Long trainerId) {

        return reservationJpaRepository.findWaitingStatus(trainerId)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getReservations(UserRole role, Long userId) {

        if (role == MEMBER) { //멤버의 경우
            return reservationJpaRepository.findByMemberId(userId)
                    .stream()
                    .map(ReservationEntity::toDomain)
                    .toList();
        }

        return reservationJpaRepository.findByTrainerId(userId)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> saveReservations(List<Reservation> reservations) {
        List<ReservationEntity> entities = reservations.stream()
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
    public Optional<Reservation> getReservation(Long reservationId, Long trainerId) {
        Optional<ReservationEntity> findEntity = reservationJpaRepository.findByIdAndTrainerIdJoinFetch(reservationId,
                trainerId);
        if (findEntity.isPresent()) {
            return findEntity.map(ReservationEntity::toDomain);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Reservation> saveReservation(Reservation reservation) {
        ReservationEntity reservationEntity = reservationJpaRepository.save(ReservationEntity.from(reservation, em));

        return Optional.of(reservationEntity.toDomain());
    }

    @Override
    public void deleteReservation(Reservation reservation) {
        reservationJpaRepository.delete(ReservationEntity.from(reservation, em));
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
    public Page<Session> getSessions(Long memberId, Long trainerId, Session.Status status, Pageable pageRequest) {
        Page<SessionEntity> result = sessionJpaRepository.findSessions(memberId, trainerId, status, pageRequest);

        return result.map(SessionEntity::toDomain);
    }

    @Override
    public Optional<Session> saveSession(Session session) {
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

    @Override
    public boolean isConfirmedReservationExists(Long trainerId, List<LocalDate> dates) {
        return reservationJpaRepository.isConfirmedReservationExists(trainerId, dates);
    }

    @Override
    public boolean isConfirmedReservationsExists(Long trainerId, List<LocalDateTime> checkDates) {
        return reservationJpaRepository.isConfirmedReservationsExists(trainerId, checkDates);
    }

    @Override
    public boolean isConfirmedReservationExists(Long trainerId, LocalDateTime checkDate) {
        return reservationJpaRepository.existsByTrainerIdAndConfirmDateTime(trainerId, checkDate);
    }
}

