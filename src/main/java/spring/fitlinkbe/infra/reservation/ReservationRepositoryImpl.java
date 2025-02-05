package spring.fitlinkbe.infra.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

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
    public Optional<Reservation> saveReservation(Reservation reservation) {
        ReservationEntity reservationEntity = reservationJpaRepository.save(ReservationEntity.from(reservation));

        return Optional.of(reservationEntity.toDomain());
    }
}
