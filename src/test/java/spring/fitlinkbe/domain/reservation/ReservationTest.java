package spring.fitlinkbe.domain.reservation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    @DisplayName("연차 정보가 있으면 true를 반환한다.")
    void isReservationNotAllowedWithDayOff() {
        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .isDayOff(true)
                .build();

        //when
        boolean result = reservation.isReservationNotAllowed();

        //then
        Assertions.assertThat(result).isTrue();
    }

    @Test
    @DisplayName("예약 불가 설정 정보가 있으면 true를 반환한다.")
    void isReservationNotAllowedWithDisabled() {
        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .isDisabled(true)
                .build();

        //when
        boolean result = reservation.isReservationNotAllowed();

        //then
        Assertions.assertThat(result).isTrue();
    }
}