package spring.fitlinkbe.domain.reservation.strategy.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TrainerCancelStrategy implements ReservationCancelStrategy {
    private final ReservationRepository reservationRepository;

    @Override
    public boolean supports(UserRole userRole) {
        return userRole == UserRole.TRAINER;
    }

    @Override
    public Reservation cancel(Reservation reservation, ReservationCommand.Cancel command) {
        LocalDateTime cancelDate = command.cancelDate();
        String cancelReason = command.cancelReason();

        reservation.cancel(cancelReason, cancelDate);
        Reservation cancelledReservation = reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_CANCEL_FAILED,
                        "예약 취소를 실패하였습니다. [reservationId: %d]".formatted(command.reservationId())));

        Session session = reservationRepository.getSession(reservation.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND,
                        "세션 정보를 찾을 수 없습니다. [reservationId: %d]".formatted(reservation.getReservationId())));

        session.cancel("트레이너의 요청으로 세션이 최소되었습니다");
        reservationRepository.saveSession(session);

        return cancelledReservation;
    }
}
