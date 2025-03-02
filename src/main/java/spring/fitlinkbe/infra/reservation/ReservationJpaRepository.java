package spring.fitlinkbe.infra.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

//    @Query("SELECT r FROM ReservationEntity r WHERE r.trainer.trainerId = :trainerId AND " +
//            "r.reservationDate BETWEEN :startDate AND :endDate")
//    List<ReservationEntity> findByTrainerAndDateRange(
//            @Param("trainerId") Long trainerId,
//            @Param("startDate") LocalDateTime startDate,
//            @Param("endDate") LocalDateTime endDate);
//
//    @Query("SELECT r FROM ReservationEntity r WHERE r.member.memberId = :memberId AND " +
//            "r.reservationDate BETWEEN :startDate AND :endDate")
//    List<ReservationEntity> findByMemberAndDateRange(
//            @Param("memberId") Long memberId,
//            @Param("startDate") LocalDateTime startDate,
//            @Param("endDate") LocalDateTime endDate);

//    @Query(value = "SELECT * FROM reservation r " +
//            "WHERE r.trainer_id = :trainerId AND " +
//            "(r.reservation_dates LIKE CONCAT('[\"', :startDate, '%') " +
//            "OR r.reservation_dates LIKE CONCAT('[\"', :endDate, '%'))",
//            nativeQuery = true)
//    List<ReservationEntity> findByTrainerAndDateRange(
//            @Param("trainerId") Long trainerId,
//            @Param("startDate") String startDate,
//            @Param("endDate") String endDate);
//
//    @Query(value = "SELECT * FROM reservation r " +
//            "WHERE r.member_id = :memberId AND " +
//            "(r.reservation_dates LIKE CONCAT('[\"', :startDate, '%') " +
//            "OR r.reservation_dates LIKE CONCAT('[\"', :endDate, '%'))",
//            nativeQuery = true)
//    List<ReservationEntity> findByMemberAndDateRange(
//            @Param("memberId") Long memberId,
//            @Param("startDate") String startDate,
//            @Param("endDate") String endDate);

    @Query("SELECT r FROM ReservationEntity r " +
            "LEFT JOIN FETCH r.member " +
            "LEFT JOIN FETCH r.trainer " +
            "WHERE r.reservationId = :reservationId")
    Optional<ReservationEntity> findByIdJoinFetch(Long reservationId);

//    @Query(value = "SELECT * FROM reservation r " +
//            "WHERE r.reservation_dates LIKE CONCAT('%', :nowDate, '%')", nativeQuery = true)
//    List<ReservationEntity> findAllReservation(@Param("nowDate") String nowDate);

    List<ReservationEntity> findByMember_MemberId(Long userId);

    List<ReservationEntity> findByTrainer_TrainerId(Long userId);

//    List<ReservationEntity> findAllReservation();

//    @Query("SELECT r FROM ReservationEntity r " +
//            "WHERE r.reservationDates > :nowDate")
//    List<ReservationEntity> findAllReservation(@Param("nowDate") LocalDateTime nowDate);
}
