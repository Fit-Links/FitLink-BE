package spring.fitlinkbe.domain.reservation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.fitlinkbe.domain.common.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
}
