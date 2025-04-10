package spring.fitlinkbe.infra.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {
    boolean isConfirmedReservationExists(Long trainerId, List<LocalDate> dates);

    boolean isConfirmedReservationsExists(Long trainerId, List<LocalDateTime> checkDates);
}
