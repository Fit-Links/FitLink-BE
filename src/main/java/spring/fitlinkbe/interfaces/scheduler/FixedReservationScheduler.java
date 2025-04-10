package spring.fitlinkbe.interfaces.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.application.reservation.ReservationFacade;

@Component
@RequiredArgsConstructor
@Slf4j
public class FixedReservationScheduler { //

    private final ReservationFacade reservationFacade;

    /**
     * 매일 정각마다 고정 예약 확인하고, 예약 실행 (일주일 뒤에 고정 예약 함)
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00에 실행
    public void CreateFixed() {
        reservationFacade.checkCreateFixedReservation();
    }

}