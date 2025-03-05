package spring.fitlinkbe.interfaces.controller.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @Builder(toBuilder = true)
    public record ReserveSession(
            @NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
            @NotNull(message = "트레이너 ID는 필수값 입니다.") Long trainerId,
            @NotBlank(message = "이름은 필수값 입니다.") String name,
            @NotEmpty(message = "예약 요청 날짜는 비어있을 수 없습니다.")
            List<LocalDateTime> dates) {

        public ReservationCriteria.ReserveSession toCriteria() {

            return ReservationCriteria.ReserveSession.builder()
                    .trainerId(trainerId)
                    .memberId(memberId)
                    .name(name)
                    .dates(dates)
                    .build();
        }

        @JsonIgnore
        @AssertTrue(message = "현재 날짜보다 이전 날짜는 설정이 불가능 합니다.")
        public boolean isNotAllowedBeforeDate() {
            if (dates == null || dates.isEmpty()) {
                return true; // 비어있는 경우는 다른 @NotEmpty에서 검증하므로 true 반환
            }
            LocalDateTime nowDate = LocalDateTime.now();
            return dates.stream().noneMatch(date -> date.isBefore(nowDate));
        }
    }
}
