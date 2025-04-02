package spring.fitlinkbe.infra.reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepositoryCustom {
    boolean isConfirmedReservationExists(Long trainerId, List<LocalDate> dates);
}
