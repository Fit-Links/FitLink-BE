package spring.fitlinkbe.infra.reservation;

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

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;
    private final SessionJpaRepository sessionJpaRepository;

    @Override
    public List<Reservation> getReservations() {

        return reservationJpaRepository.findAllReservation(LocalDateTime.now())
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> getReservations(LocalDateTime startDate, LocalDateTime endDate,
                                             UserRole role, Long userId) {

        if (role == MEMBER) { //멤버의 경우
            return reservationJpaRepository.findByMemberAndDateRange(userId,
                            startDate, endDate)
                    .stream()
                    .map(ReservationEntity::toDomain)
                    .toList();
        }

        return reservationJpaRepository.findByTrainerAndDateRange(userId,
                        startDate, endDate)
                .stream()
                .map(ReservationEntity::toDomain)
                .toList();
    }

    @Override
    public List<Reservation> cancelReservations(List<Reservation> canceledReservations) {
        List<ReservationEntity> entities = canceledReservations.stream()
                .map(ReservationEntity::from)
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
    public Optional<Reservation> saveReservation(Reservation reservation) {
        ReservationEntity reservationEntity = reservationJpaRepository.save(ReservationEntity.from(reservation));

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
    public Optional<Session> saveSession(Session session) {
        SessionEntity savedEntity = sessionJpaRepository.save(SessionEntity.from(session));

        return Optional.of(savedEntity.toDomain());
    }

    @Override
    public List<Session> cancelSessions(List<Session> sessions) {
        List<SessionEntity> entities = sessions.stream()
                .map(SessionEntity::from)
                .toList();

        List<SessionEntity> savedEntities = sessionJpaRepository.saveAll(entities);

        return savedEntities.stream()
                .map(SessionEntity::toDomain)
                .toList();
    }

}
