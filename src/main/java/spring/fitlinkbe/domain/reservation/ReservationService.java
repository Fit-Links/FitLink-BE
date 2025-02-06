package spring.fitlinkbe.domain.reservation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<Reservation> getReservations(LocalDate date, UserRole role, Long userId) {

        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = Reservation.getEndDate(startDate, role);

        return reservationRepository.getReservations(startDate, endDate, role, userId);
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(Long reservationId) {
        return reservationRepository.getReservation(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
    }

    @Transactional(readOnly = true)
    public Session getSession(boolean isApproved, Long reservationId) {
        Optional<Session> getSession = reservationRepository.getSession(reservationId);
        // 예약 승낙 전에는 세션 정보는 없다.
        if (!isApproved) {
            return null;
        }

        return getSession.orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservationId)));
    }
}
