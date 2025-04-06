package spring.fitlinkbe.infra.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long>, ReservationRepositoryCustom {
    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.reservationId = :reservationId")
    Optional<ReservationEntity> findByIdJoinFetch(Long reservationId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.member.memberId = :memberId " +
            "AND r.createdAt > CURRENT_TIMESTAMP")
    List<ReservationEntity> findByMember_MemberId(Long memberId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.trainer.trainerId = :trainerId " +
            "AND r.createdAt > CURRENT_TIMESTAMP")
    List<ReservationEntity> findByTrainer_TrainerId(Long trainerId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.trainer.trainerId = :trainerId " +
            "AND r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_WAITING " +
            "AND r.createdAt > CURRENT_TIMESTAMP")
    List<ReservationEntity> findWaitingStatus(Long trainerId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.createdAt > CURRENT_TIMESTAMP")
    List<ReservationEntity> findAllAfterToday();

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION " +
            "AND r.createdAt > CURRENT_TIMESTAMP")
    List<ReservationEntity> findFixedStatus();
}
