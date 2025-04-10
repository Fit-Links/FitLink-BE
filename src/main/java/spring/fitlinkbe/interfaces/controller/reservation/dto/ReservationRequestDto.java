package spring.fitlinkbe.interfaces.controller.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Builder;
import spring.fitlinkbe.application.reservation.criteria.ReservationCriteria;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    public record Create(
            @NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
            @NotNull(message = "트레이너 ID는 필수값 입니다.") Long trainerId,
            @NotBlank(message = "이름은 필수값 입니다.") String name,
            @NotEmpty(message = "예약 요청 날짜는 비어있을 수 없습니다.")
            @NotAllowedBeforeDate
            List<LocalDateTime> dates) {

        public ReservationCriteria.Create toCriteria() {

            return ReservationCriteria.Create.builder()
                    .trainerId(trainerId)
                    .memberId(memberId)
                    .name(name)
                    .dates(dates)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record CreateFixed(
            @NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
            @NotBlank(message = "이름은 필수값 입니다.") String name,
            @NotEmpty(message = "예약 요청 날짜는 비어있을 수 없습니다.")
            @NotAllowedBeforeDate
            List<LocalDateTime> dates) {

        public ReservationCriteria.CreateFixed toCriteria() {

            return ReservationCriteria.CreateFixed.builder()
                    .memberId(memberId)
                    .name(name)
                    .reservationDates(dates)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record Cancel(@NotEmpty(message = "취소 사유는 필수값 입니다.") String cancelReason,
                         @NotNull(message = "취소 날짜는 필수입니다.")
                         @FutureOrPresent(message = "현재 날짜보다 이전일 수 없습니다.")
                         LocalDateTime cancelDate) {

        public ReservationCriteria.Cancel toCriteria(Long reservationId) {

            return ReservationCriteria.Cancel.builder()
                    .reservationId(reservationId)
                    .cancelDate(cancelDate)
                    .cancelReason(cancelReason)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record CancelApproval(@NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
                                 @NotNull(message = "승인 여부는 필수값 입니다.") Boolean isApprove) {
        public ReservationCriteria.CancelApproval toCriteria(Long reservationId) {

            return ReservationCriteria.CancelApproval.builder()
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .isApprove(isApprove)
                    .build();

        }
    }

    @Builder(toBuilder = true)
    public record Approve(@NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
                          @NotNull(message = "요청 날짜는 비어있을 수 없습니다.")
                          @FutureOrPresent(message = "현재 날짜보다 이전일 수 없습니다.")
                          LocalDateTime reservationDate) {
        public ReservationCriteria.Approve toCriteria(Long reservationId) {

            return ReservationCriteria.Approve.builder()
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .reservationDate(reservationDate)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record ChangeReqeust(@NotNull(message = "예약 날짜는 필수입니다.")
                                @FutureOrPresent(message = "현재 날짜보다 이전일 수 없습니다.")
                                LocalDateTime reservationDate,
                                @NotNull(message = "변경 날짜는 필수입니다.")
                                @FutureOrPresent(message = "현재 날짜보다 이전일 수 없습니다.")
                                LocalDateTime changeRequestDate) {

        @JsonIgnore
        @AssertTrue(message = "예약 날짜와 변경 날짜가 같을 수 없습니다.")
        private boolean isSameDate() {
            if (reservationDate == null || changeRequestDate == null) {
                return true;
            }
            return !reservationDate.truncatedTo(ChronoUnit.HOURS).isEqual(changeRequestDate.truncatedTo(ChronoUnit.HOURS));
        }

        public ReservationCriteria.ChangeReqeust toCriteria(Long reservationId) {
            return ReservationCriteria.ChangeReqeust.builder()
                    .reservationId(reservationId)
                    .reservationDate(reservationDate)
                    .changeRequestDate(changeRequestDate)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record ChangeApproval(@NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
                                 @NotNull(message = "승인 날짜는 필수입니다.")
                                 @FutureOrPresent(message = "현재 날짜보다 이전일 수 없습니다.")
                                 LocalDateTime approveDate,
                                 @NotNull(message = "승인 여부는 필수값 입니다.") Boolean isApprove) {

        public ReservationCriteria.ChangeApproval toCriteria(Long reservationId) {
            return ReservationCriteria.ChangeApproval.builder()
                    .reservationId(reservationId)
                    .approveDate(approveDate)
                    .memberId(memberId)
                    .isApprove(isApprove)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record Complete(@NotNull(message = "유저 ID는 필수값 입니다.") Long memberId,
                           @NotNull(message = "참석 여부는 필수값 입니다.") Boolean isJoin) {

        public ReservationCriteria.Complete toCriteria(Long reservationId) {
            return ReservationCriteria.Complete.builder()
                    .reservationId(reservationId)
                    .memberId(memberId)
                    .isJoin(isJoin)
                    .build();
        }
    }

}
