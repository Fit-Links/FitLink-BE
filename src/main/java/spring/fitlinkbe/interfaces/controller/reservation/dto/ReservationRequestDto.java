package spring.fitlinkbe.interfaces.controller.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;

import java.time.LocalDateTime;

public class ReservationRequestDto {

    @Builder(toBuilder = true)
    public record SetDisabledTime(LocalDateTime date, Long trainerId) {

        public ReservationCriteria.SetDisabledTime toCriteria(Long trainerId) {
            return ReservationCriteria.SetDisabledTime.builder()
                    .trainerId(trainerId)
                    .date(date)
                    .build();
        }

        @JsonIgnore
        @AssertTrue(message = "현재 날짜보다 이전 날짜는 설정이 불가능 합니다.")
        private boolean isNotAllowedBeforeDate() {
            if (date == null) {
                return false;
            }
            LocalDateTime nowDate = LocalDateTime.now();

            return nowDate.isBefore(date);
        }

    }

    @Builder(toBuilder = true)
    public record ReserveSession(@NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
                                 @NotBlank(message = "이름은 필수값 입니다.") String name,
                                 LocalDateTime date, int priority) {

        public ReservationCriteria.ReserveSession toCriteria() {

            return ReservationCriteria.ReserveSession.builder()
                    .memberId(memberId)
                    .name(name)
                    .date(date)
                    .priority(priority)
                    .build();
        }

        @JsonIgnore
        @AssertTrue(message = "현재 날짜보다 이전 날짜는 설정이 불가능 합니다.")
        private boolean isNotAllowedBeforeDate() {
            if (date == null) {
                return false;
            }
            LocalDateTime nowDate = LocalDateTime.now();

            return nowDate.isBefore(date);
        }
    }
}
