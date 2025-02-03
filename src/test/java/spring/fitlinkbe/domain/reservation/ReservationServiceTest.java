package spring.fitlinkbe.domain.reservation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.support.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

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

}