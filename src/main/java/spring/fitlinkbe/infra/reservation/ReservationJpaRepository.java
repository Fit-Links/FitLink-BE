package spring.fitlinkbe.infra.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
            "WHERE r.member.memberId = :userId")
    List<ReservationEntity> findByMember_MemberId(Long userId);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "LEFT JOIN FETCH r.sessionInfo " +
            "WHERE r.trainer.trainerId = :userId")
    List<ReservationEntity> findByTrainer_TrainerId(Long userId);

}
