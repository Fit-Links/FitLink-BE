package spring.fitlinkbe.domain.reservation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.support.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.RESERVATION_NOT_FOUND;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("트레이너의 예약 목록을 반환한다.")
    void getReservationsWithTrainer() {

        //given
        LocalDate startDdate = LocalDate.of(2024, 4, 20);
        LocalDateTime endDate = DateUtils.getTwoWeekAfterDate(startDdate.atStartOfDay());
        LocalDateTime reservationDate = startDdate.atStartOfDay().plusDays(2L);

        Reservation reservation = Reservation.builder()
                .reservationDate(reservationDate)
                .build();

        when(reservationRepository.getReservations(startDdate.atStartOfDay(), endDate,
                UserRole.TRAINER, 1L))
                .thenReturn(List.of(reservation));

        //when
        List<Reservation> result = reservationService.getReservations(startDdate, UserRole.TRAINER, 1L);

        //then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("멤버의 예약 목록을 반환한다.")
    void getReservationsWithMember() {

        //given
        LocalDate startDdate = LocalDate.of(2024, 4, 20);
        LocalDateTime endDate = DateUtils.getOneMonthAfterDate(startDdate.atStartOfDay());
        LocalDateTime reservationDate = startDdate.atStartOfDay().plusMonths(1L).minusSeconds(1L);

        Reservation reservation = Reservation.builder()
                .reservationDate(reservationDate)
                .build();

        when(reservationRepository.getReservations(startDdate.atStartOfDay(), endDate, UserRole.MEMBER, 1L))
                .thenReturn(List.of(reservation));

        //when
        List<Reservation> result = reservationService.getReservations(startDdate, UserRole.MEMBER, 1L);

        //then
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("예약 목록이 없으면 빈 배열을 반환한다.")
    void getReservationsWithEmpty() {

        //given
        LocalDate startDdate = LocalDate.of(2024, 4, 20);
        LocalDateTime endDate = DateUtils.getOneMonthAfterDate(startDdate.atStartOfDay());

        when(reservationRepository.getReservations(startDdate.atStartOfDay(), endDate, UserRole.TRAINER, 1L))
                .thenReturn(List.of());

        //when
        List<Reservation> result = reservationService.getReservations(startDdate, UserRole.TRAINER, 1L);

        //then
        Assertions.assertThat(result).hasSize(0);
    }

    @Test
    @DisplayName("트레이너의 예약 상세 정보를 반환한다.")
    void getReservation() {

        //given
        Reservation reservation = Reservation.builder()
                .reservationId(1L)
                .build();

        when(reservationRepository.getReservation(reservation.getReservationId()))
                .thenReturn(Optional.of(reservation));

        //when
        Reservation result = reservationService.getReservation(reservation.getReservationId());

        //then
        Assertions.assertThat(result.getReservationId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("없는 예약 정보를 조회하면 RESERVATION_NOT_FOUND 예외를 반환한다.")
    void getReservationWithNotFound() {

        //given
        Long reservationId = 1000L;

        when(reservationRepository.getReservation(any(Long.class))).thenThrow(
                new CustomException(RESERVATION_NOT_FOUND,
                        RESERVATION_NOT_FOUND.getMsg()));

        //when & then
        assertThatThrownBy(() -> reservationService.getReservation(reservationId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(RESERVATION_NOT_FOUND);
    }

    @Test
    @DisplayName("세션 정보를 반환한다.")
    void getSession() {
        //given
        Session session = Session.builder()
                .reservation(Reservation.builder().reservationId(10L).build())
                .sessionId(1L)
                .build();

        when(reservationRepository.getSession(session.getReservation().getReservationId()))
                .thenReturn(Optional.of(session));

        //when
        Session result = reservationService.getSession(true, session.getReservation().getReservationId());

        //then
        Assertions.assertThat(result.getSessionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("예약 승낙이 나지 않았다면 세션 정보는 null을 반환한다.")
    void getSessionWithEmpty() {
        //given
        Long reservationId = 1L;

        when(reservationRepository.getSession(reservationId))
                .thenReturn(Optional.empty());

        //when
        Session result = reservationService.getSession(false, reservationId);

        //then
        Assertions.assertThat(result).isNull();
    }

}