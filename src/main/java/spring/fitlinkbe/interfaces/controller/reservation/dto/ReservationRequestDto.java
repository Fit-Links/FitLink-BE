package spring.fitlinkbe.interfaces.controller.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationRequestDto {

    @Builder(toBuilder = true)
    public record SetDisabledTime(LocalDateTime date, Long trainerId) {

        public ReservationCriteria.SetDisabledTime toCriteria() {
            return ReservationCriteria.SetDisabledTime.builder()
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

    @Builder
    public record ReserveSessions(
            @NotEmpty(message = "예약 요청 리스트는 비어있을 수 없습니다.")
            @Valid List<ReserveSession> reserveSessions) {

        @Builder(toBuilder = true)
        public record ReserveSession(
                @NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
                @NotNull(message = "트레이너 ID는 필수값 입니다.") Long trainerId,
                @NotBlank(message = "이름은 필수값 입니다.") String name,
                LocalDateTime date, int priority) {

            public ReservationCriteria.ReserveSession toCriteria() {

                return ReservationCriteria.ReserveSession.builder()
                        .trainerId(trainerId)
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


}
