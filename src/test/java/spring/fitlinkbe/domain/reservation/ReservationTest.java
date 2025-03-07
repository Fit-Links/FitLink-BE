package spring.fitlinkbe.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import spring.fitlinkbe.domain.common.exception.CustomException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.RESERVATION_IS_ALREADY_CANCEL;
import static spring.fitlinkbe.domain.reservation.Reservation.Status;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_CANCELLED;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.RESERVATION_WAITING;
import static spring.fitlinkbe.domain.reservation.Reservation.builder;

class ReservationTest {

    @Test
    @DisplayName("연차 정보가 있으면 true를 반환한다.")
    void isReservationNotAllowedWithDayOff() {
        //given
        Reservation reservation = builder()
                .reservationId(1L)
                .isDayOff(true)
                .build();

        //when
        boolean result = reservation.isReservationNotAllowed();

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("예약 불가 설정 정보가 있으면 true를 반환한다.")
    void isReservationNotAllowedWithDisabled() {
        //given
        Reservation reservation = builder()
                .reservationId(1L)
                .status(Status.DISABLED_TIME_RESERVATION)
                .build();

        //when
        boolean result = reservation.isReservationNotAllowed();

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("예약을 취소합니다.")
    void cancelReservation() {
        //given
        Reservation reservation = builder()
                .reservationId(1L)
                .status(RESERVATION_WAITING)
                .build();

        //when
        reservation.cancel("예약 불가 설정");

        //then
        assertThat(reservation.getStatus()).isEqualTo(RESERVATION_CANCELLED);
    }

    @Test
    @DisplayName("이미 취소된 예약은 취소할 수 없습니다.")
    void cancelReservationWithAlreadyCancelled() {
        //given
        Reservation reservation = builder()
                .reservationId(1L)
                .status(RESERVATION_CANCELLED)
                .build();

        //when & then
        assertThatThrownBy(() -> reservation.cancel("이미 취소된 예약입니다."))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(RESERVATION_IS_ALREADY_CANCEL);
    }


}