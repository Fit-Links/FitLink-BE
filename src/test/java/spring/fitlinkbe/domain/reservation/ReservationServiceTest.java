package spring.fitlinkbe.domain.reservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.common.exception.CustomException;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static spring.fitlinkbe.domain.common.exception.ErrorCode.*;
import static spring.fitlinkbe.domain.reservation.Reservation.Status.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("예약 목록 조회 Service TEST")
    class GetReservationsServiceTest {

        @Test
        @DisplayName("트레이너의 예약 목록을 반환한다.")
        void getReservationsWithTrainer() {

            //given
            LocalDate startDdate = LocalDate.of(2024, 4, 20);
            LocalDateTime reservationDate = startDdate.atStartOfDay().plusDays(2L);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .build();

            when(reservationRepository.getReservations(UserRole.TRAINER, 1L))
                    .thenReturn(List.of(reservation));

            //when
            List<Reservation> result = reservationService.getReservations(startDdate, UserRole.TRAINER, 1L);

            //then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("멤버의 예약 목록을 반환한다.")
        void getReservationsWithMember() {

            //given
            LocalDate startDdate = LocalDate.of(2024, 4, 20);
            LocalDateTime reservationDate = startDdate.atStartOfDay().plusMonths(1L).minusSeconds(2L);

            Reservation reservation = Reservation.builder()
                    .reservationDates(List.of(reservationDate))
                    .build();

            when(reservationRepository.getReservations(UserRole.MEMBER, 1L))
                    .thenReturn(List.of(reservation));

            //when
            List<Reservation> result = reservationService.getReservations(startDdate, UserRole.MEMBER, 1L);

            //then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("예약 목록이 없으면 빈 배열을 반환한다.")
        void getReservationsWithEmpty() {

            //given
            LocalDate startDdate = LocalDate.of(2024, 4, 20);

            when(reservationRepository.getReservations(UserRole.TRAINER, 1L))
                    .thenReturn(List.of());

            //when
            List<Reservation> result = reservationService.getReservations(startDdate, UserRole.TRAINER, 1L);

            //then
            assertThat(result).hasSize(0);
        }
    }

    @Nested
    @DisplayName("예약 상세 조회 Service TEST")
    class GetReservationServiceTest {
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
            assertThat(result.getReservationId()).isEqualTo(1L);
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
                    .reservationId(1L)
                    .sessionId(1L)
                    .build();

            when(reservationRepository.getSession(session.getReservationId()))
                    .thenReturn(Optional.of(session));

            //when
            Session result = reservationService.getSession(
                    RESERVATION_APPROVED, session.getReservationId());

            //then
            assertThat(result.getSessionId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("예약 승낙이 나지 않았다면 세션 정보는 null을 반환한다.")
        void getSessionWithEmpty() {
            //given
            Long reservationId = 1L;

            when(reservationRepository.getSession(reservationId))
                    .thenReturn(Optional.empty());

            //when
            Session result = reservationService.getSession(RESERVATION_WAITING, reservationId);

            //then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("예약 불가 설정 Service TEST")
    class SetDisabledTimeServiceTest {
        @Test
        @DisplayName("예약을 취소하면 RESERVATION_CANCELLED 상태가 됩니다.")
        void cancelReservations() {
            //given
            Reservation reservation = Reservation
                    .builder()
                    .status(RESERVATION_APPROVED)
                    .build();

            Reservation canceledReservation = Reservation.builder()
                    .status(RESERVATION_CANCELLED)
                    .build();

            String message = "예약 불가 설정";

            when(reservationRepository.cancelReservations(List.of(reservation))).thenReturn(List.of(canceledReservation));

            //when
            reservationService.cancelReservations(List.of(reservation), message);

            //then
            assertThat(reservation.getStatus()).isEqualTo(RESERVATION_CANCELLED);
        }

        @Test
        @DisplayName("확정된 예약을 취소하면 RESERVATION_CANCELLED 상태가 되고, 세션도 같이 취소됩니다.")
        void cancelReservationWithSession() {
            //given
            Reservation reservation = Reservation
                    .builder()
                    .status(RESERVATION_APPROVED)
                    .build();

            Reservation canceledReservation = Reservation.builder()
                    .status(RESERVATION_CANCELLED)
                    .build();

            String message = "예약 불가 설정";

            when(reservationRepository.cancelReservations(List.of(reservation))).thenReturn(List.of(canceledReservation));

            //when
            reservationService.cancelReservations(List.of(reservation), message);

            //then
            assertThat(reservation.getStatus()).isEqualTo(RESERVATION_CANCELLED);
        }

        @Test
        @DisplayName("이미 예약이 취소되었다면 RESERVATION_IS_ALREADY_CANCEL 예외를 반환합니다.")
        void cancelReservationsWithAlreadyCancelled() {
            //given
            Reservation reservation = Reservation
                    .builder()
                    .status(RESERVATION_CANCELLED)
                    .build();

            String message = "예약 불가 설정";

            //when & then
            assertThatThrownBy(() -> reservationService.cancelReservations(List.of(reservation), message))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(RESERVATION_IS_ALREADY_CANCEL);
        }

        @Test
        @DisplayName("예약 불가 시간을 설정합니다.")
        void setDisabledTime() {
            //given
            ReservationCommand.SetDisabledTime command = ReservationCommand
                    .SetDisabledTime.builder()
                    .date(LocalDateTime.parse("2024-10-14T10:00"))
                    .trainerId(1L)
                    .build();

            Trainer trainer = Trainer.builder().trainerId(1L).build();

            Reservation savedReservation = Reservation.builder()
                    .trainer(trainer)
                    .status(DISABLED_TIME_RESERVATION)
                    .build();

            when(reservationRepository.reserveSession(any(Reservation.class)))
                    .thenReturn(Optional.ofNullable(savedReservation));

            //when
            Reservation result = reservationService.setDisabledTime(command, trainer);

            //then
            assertThat(result.getStatus()).isEqualTo(DISABLED_TIME_RESERVATION);
        }

    }

    @Nested
    @DisplayName("세션 예약 Service TEST")
    class ReserveSessionServiceTest {

        @Test
        @DisplayName("세션 예약 성공")
        void reserveSession() {
            //given
            Reservation reservation = Reservation.builder()
                    .status(RESERVATION_WAITING)
                    .build();

            Reservation savedReservation = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_WAITING)
                    .build();

            when(reservationRepository.reserveSession(reservation))
                    .thenReturn(Optional.ofNullable(savedReservation));

            //when
            Reservation result = reservationService.reserveSession(reservation);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getReservationId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("세션 예약 실패 - 예약 정보가 안넘어 왔을 때")
        void reserveSessionNoReservationInfo() {
            //given
            when(reservationRepository.reserveSession(any(Reservation.class))).thenThrow(
                    new CustomException(RESERVATION_NOT_FOUND,
                            RESERVATION_NOT_FOUND.getMsg()));

            //when & then
            assertThatThrownBy(() -> reservationService.reserveSession(Reservation.builder().build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(RESERVATION_NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("세션 생성 Service TEST")
    class CreateSessionServiceTest {

        @Test
        @DisplayName("세션 생성 성공")
        void createSession() {
            //given
            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_WAITING)
                    .build();

            Session session = Session.builder()
                    .sessionId(1L)
                    .build();

            when(reservationRepository.createSession(any(Session.class)))
                    .thenReturn(Optional.ofNullable(session));

            //when
            Session result = reservationService.createSession(reservation);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("세션 생성 실패 - 예약 정보가 안넘어 왔을 때")
        void createSessionNoReservationInfo() {
            //given
            when(reservationRepository.createSession(any())).thenThrow(
                    new CustomException(SESSION_CREATE_FAILED,
                            SESSION_CREATE_FAILED.getMsg()));

            //when & then
            assertThatThrownBy(() -> reservationService.createSession(Reservation.builder().build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(SESSION_CREATE_FAILED);
        }
    }
}