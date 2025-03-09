package spring.fitlinkbe.interfaces.controller.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationRequestDto {

    @Builder(toBuilder = true)
    public record SetDisabledTime(@NotNull(message = "예약 날짜는 필수입니다.")
                                  @FutureOrPresent(message = "현재 날짜보다 이전일 수 없습니다.")
                                  LocalDateTime date) {

        public ReservationCriteria.SetDisabledTime toCriteria() {
            return ReservationCriteria.SetDisabledTime.builder()
                    .date(date)
                    .build();
        }
    }


    @Builder(toBuilder = true)
    public record ReserveSession(
            @NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
            @NotNull(message = "트레이너 ID는 필수값 입니다.") Long trainerId,
            @NotBlank(message = "이름은 필수값 입니다.") String name,
            @NotEmpty(message = "예약 요청 날짜는 비어있을 수 없습니다.")
            @NotAllowedBeforeDate
            List<LocalDateTime> dates) {

        public ReservationCriteria.ReserveSession toCriteria() {

            return ReservationCriteria.ReserveSession.builder()
                    .trainerId(trainerId)
                    .memberId(memberId)
                    .name(name)
                    .dates(dates)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record FixedReserveSession(
            @NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
            @NotBlank(message = "이름은 필수값 입니다.") String name,
            @NotEmpty(message = "예약 요청 날짜는 비어있을 수 없습니다.")
            @NotAllowedBeforeDate
            List<LocalDateTime> dates) {

        public ReservationCriteria.FixedReserveSession toCriteria() {

            return ReservationCriteria.FixedReserveSession.builder()
                    .memberId(memberId)
                    .name(name)
                    .reservationDates(dates)
                    .build();
        }
    }
}
