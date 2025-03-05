package spring.fitlinkbe.application.reservation.criteria;

import lombok.Builder;
import spring.fitlinkbe.domain.common.model.SessionInfo;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDateTime;
import java.util.List;

import static spring.fitlinkbe.domain.common.enums.UserRole.MEMBER;

public class ReservationCriteria {

    @Builder(toBuilder = true)
    public record SetDisabledTime(LocalDateTime date) {

        public ReservationCommand.SetDisabledTime toCommand() {
            return ReservationCommand.SetDisabledTime.builder()
                    .date(date)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record ReserveSession(Long trainerId, Long memberId, String name, List<LocalDateTime> dates) {
        public Reservation toDomain(SessionInfo sessionInfo, SecurityUser user) {

            return Reservation.builder()
                    .trainer(Trainer.builder().trainerId(trainerId).build())
                    .member(Member.builder().memberId(memberId).build())
                    .sessionInfo(sessionInfo)
                    .name(name)
                    .reservationDates(dates)
                    .dayOfWeek(dates.get(0).getDayOfWeek())
                    .status(user.getUserRole() == MEMBER ? Reservation.Status.RESERVATION_WAITING
                            : Reservation.Status.RESERVATION_APPROVED)
                    .build();
        }
    }
}
