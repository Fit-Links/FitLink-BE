package spring.fitlinkbe.infra.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

    @Query("SELECT r FROM ReservationEntity r WHERE r.trainer.trainerId = :trainerId AND " +
            "r.reservationDate BETWEEN :startDate AND :endDate")
    List<ReservationEntity> findByTrainerAndDateRange(
            @Param("trainerId") Long trainerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM ReservationEntity r WHERE r.member.memberId = :memberId AND " +
            "r.reservationDate BETWEEN :startDate AND :endDate")
    List<ReservationEntity> findByMemberAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "WHERE r.reservationId = :reservationId")
    Optional<ReservationEntity> findByIdJoinFetch(Long reservationId);
}
