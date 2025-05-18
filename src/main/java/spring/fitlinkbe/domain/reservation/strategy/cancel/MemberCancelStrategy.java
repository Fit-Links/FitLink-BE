package spring.fitlinkbe.domain.reservation.strategy.cancel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.common.exception.ErrorCode;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.ReservationRepository;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MemberCancelStrategy implements ReservationCancelStrategy {
    private final ReservationRepository reservationRepository;

    @Override
    public boolean supports(UserRole userRole) {
        return userRole == UserRole.MEMBER;
    }

    @Override
    public Reservation cancel(Reservation reservation, ReservationCommand.Cancel command) {
        LocalDateTime cancelDate = command.cancelDate();
        String cancelReason = command.cancelReason();

        reservation.cancelRequest(cancelReason, cancelDate);

        return reservationRepository.saveReservation(reservation)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_CANCEL_FAILED,
                        "예약 취소를 실패하였습니다. [reservationId: %d]".formatted(command.reservationId())));
    }
}
