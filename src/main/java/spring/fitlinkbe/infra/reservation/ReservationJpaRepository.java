package spring.fitlinkbe.infra.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import spring.fitlinkbe.domain.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {
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
            "WHERE r.member.memberId = :userId " +
            "AND r.createdAt > :now")
    List<ReservationEntity> findByMember_MemberId(Long userId, LocalDateTime now);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.trainer.trainerId = :userId " +
            "AND r.createdAt > :now")
    List<ReservationEntity> findByTrainer_TrainerId(Long userId, LocalDateTime now);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.trainer.trainerId = :trainerId " +
            "AND r.status = :status " +
            "AND r.createdAt > :now")
    List<ReservationEntity> findWaitingStatus(Reservation.Status status,
                                              Long trainerId, LocalDateTime now);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.createdAt > :now")
    List<ReservationEntity> findAllAfterToday(LocalDateTime now);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.status = :status " +
            "AND r.createdAt > :now")
    List<ReservationEntity> findFixedStatus(Reservation.Status status,
                                            LocalDateTime now);
}
