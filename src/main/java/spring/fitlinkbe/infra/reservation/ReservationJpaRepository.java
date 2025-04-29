package spring.fitlinkbe.infra.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
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
            "WHERE r.reservationId = :reservationId AND r.trainer.trainerId = :trainerId")
    Optional<ReservationEntity> findByIdAndTrainerIdJoinFetch(Long reservationId, Long trainerId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.member.memberId = :memberId")
    List<ReservationEntity> findByMemberId(Long memberId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.trainer.trainerId = :trainerId")
    List<ReservationEntity> findByTrainerId(Long trainerId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.trainer.trainerId = :trainerId " +
            "AND r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_WAITING")
    List<ReservationEntity> findWaitingStatus(Long trainerId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo")
    List<ReservationEntity> findAllJoinFetch();

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION")
    List<ReservationEntity> findFixedStatus();

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION " +
            "AND r.member.memberId = :memberId")
    List<ReservationEntity> findFixedStatus(Long memberId);

    @Query("SELECT COUNT(r) > 0 FROM ReservationEntity r " +
            "WHERE r.trainer.trainerId = :trainerId " +
            "AND r.confirmDate = :checkDateTime " +
            "AND (r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION " +
            "OR r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_APPROVED)")
    boolean existsByTrainerIdAndConfirmDateTime(Long trainerId, LocalDateTime checkDateTime);

    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.trainer.trainerId = :trainerId " +
            "AND r.confirmDate = :checkDateTime " +
            "AND r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION")
    List<ReservationEntity> findAllFixedReservation(Long trainerId, LocalDateTime checkDateTime);

    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.member.memberId = :memberId " +
            "AND r.status = spring.fitlinkbe.domain.reservation.Reservation.Status.FIXED_RESERVATION")
    List<ReservationEntity> findAllFixedReservation(Long memberId);
}
