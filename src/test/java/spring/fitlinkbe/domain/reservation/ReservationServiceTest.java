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
import spring.fitlinkbe.domain.common.model.PersonalDetail;
import spring.fitlinkbe.domain.member.Member;
import spring.fitlinkbe.domain.reservation.command.ReservationCommand;
import spring.fitlinkbe.domain.trainer.Trainer;
import spring.fitlinkbe.support.security.SecurityUser;

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
            List<Reservation> result = reservationService.getReservations(
                    ReservationCommand.GetReservations.of(startDdate, UserRole.TRAINER, 1L));

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
            List<Reservation> result = reservationService.getReservations(
                    ReservationCommand.GetReservations.of(startDdate, UserRole.MEMBER, 1L));

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
            List<Reservation> result = reservationService.getReservations(
                    ReservationCommand.GetReservations.of(startDdate, UserRole.TRAINER, 1L));

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
                    .reservation(Reservation.builder().reservationId(1L).build())
                    .sessionId(1L)
                    .build();

            when(reservationRepository.getSession(session.getReservation().getReservationId()))
                    .thenReturn(Optional.of(session));

            //when
            Session result = reservationService.getSession(
                    RESERVATION_APPROVED, session.getReservation().getReservationId());

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
    class SetDisabledReservationTest {
        @Test
        @DisplayName("예약 불가 설정 성공")
        void setDisabledReservation() {
            //given
            LocalDateTime requestDate = LocalDateTime.now();

            ReservationCommand.SetDisabledTime command = ReservationCommand.SetDisabledTime.builder()
                    .trainerId(1L)
                    .date(requestDate)
                    .build();


            Reservation disabledReservation = Reservation.builder()
                    .status(DISABLED_TIME_RESERVATION)
                    .build();

            when(reservationRepository.saveReservation(any(Reservation.class))).thenReturn(Optional.ofNullable(disabledReservation));

            //when
            Reservation result = reservationService.setDisabledReservation(command);

            //then
            assertThat(result.getStatus()).isEqualTo(DISABLED_TIME_RESERVATION);
        }

    }

    @Nested
    @DisplayName("세션 예약 Service TEST")
    class CreateReservationServiceTest {

        @Test
        @DisplayName("세션 예약 성공")
        void createReservation() {
            //given
            Reservation reservation = Reservation.builder()
                    .status(RESERVATION_WAITING)
                    .build();

            Reservation savedReservation = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_WAITING)
                    .build();

            when(reservationRepository.saveReservation(reservation))
                    .thenReturn(Optional.ofNullable(savedReservation));

            //when
            Reservation result = reservationService.createReservation(reservation);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getReservationId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("세션 예약 실패 - 예약 정보가 안넘어 왔을 때")
        void createReservationNoReservationInfo() {
            //given
            when(reservationRepository.saveReservation(any(Reservation.class))).thenThrow(
                    new CustomException(RESERVATION_NOT_FOUND,
                            RESERVATION_NOT_FOUND.getMsg()));

            //when & then
            assertThatThrownBy(() -> reservationService.createReservation(Reservation.builder().build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(RESERVATION_NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("세션 생성 Service TEST")
    class SaveSessionServiceTest {

        @Test
        @DisplayName("세션 생성 성공")
        void saveSession() {
            //given
            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .status(RESERVATION_WAITING)
                    .build();

            Session session = Session.builder()
                    .sessionId(1L)
                    .build();

            when(reservationRepository.saveSession(any(Session.class)))
                    .thenReturn(Optional.ofNullable(session));

            //when
            Session result = reservationService.saveSession(reservation);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("세션 생성 실패 - 예약 정보가 안넘어 왔을 때")
        void saveSessionNoReservationInfo() {
            //given
            when(reservationRepository.saveSession(any())).thenThrow(
                    new CustomException(SESSION_CREATE_FAILED,
                            SESSION_CREATE_FAILED.getMsg()));

            //when & then
            assertThatThrownBy(() -> reservationService.saveSession(Reservation.builder().build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(SESSION_CREATE_FAILED);
        }
    }

    @Nested
    @DisplayName("세션 완료 Service TEST")
    class CompleteSessionServiceTest {

        @Test
        @DisplayName("세션 완료 성공")
        void completeSession() {
            //given
            ReservationCommand.Complete command = ReservationCommand.Complete.builder()
                    .reservationId(1L)
                    .memberId(1L)
                    .isJoin(true)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            Session session = Session.builder()
                    .sessionId(1L)
                    .status(Session.Status.SESSION_WAITING)
                    .build();

            Session completedSession = Session.builder()
                    .sessionId(1L)
                    .status(Session.Status.SESSION_COMPLETED)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .build();

            Reservation completedReservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_COMPLETED)
                    .build();

            when(reservationRepository.getSession(command.reservationId()))
                    .thenReturn(Optional.ofNullable(session));

            when(reservationRepository.saveSession(session))
                    .thenReturn(Optional.ofNullable(completedSession));

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            when(reservationRepository.saveReservation(reservation))
                    .thenReturn(Optional.ofNullable(completedReservation));

            //when
            Session result = reservationService.completeSession(command, user);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getSessionId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(Session.Status.SESSION_COMPLETED);
        }

        @Test
        @DisplayName("세션 완료 실패 - 세션 정보 없음")
        void completeSessionNoSessionInfo() {
            //given
            ReservationCommand.Complete command = ReservationCommand.Complete.builder()
                    .reservationId(1L)
                    .isJoin(true)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            when(reservationRepository.getSession(any(Long.class))).thenThrow(
                    new CustomException(SESSION_NOT_FOUND));

            //when & then
            assertThatThrownBy(() -> reservationService.completeSession(command, user))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(SESSION_NOT_FOUND);
        }

        @Test
        @DisplayName("세션 완료 실패 - 이미 세션 완료")
        void completeSessionAlreadySessionCompleted() {
            //given
            ReservationCommand.Complete command = ReservationCommand.Complete.builder()
                    .reservationId(1L)
                    .isJoin(true)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            Session session = Session.builder()
                    .sessionId(1L)
                    .status(Session.Status.SESSION_COMPLETED)
                    .build();


            when(reservationRepository.getSession(command.reservationId()))
                    .thenReturn(Optional.ofNullable(session));

            //when & then
            assertThatThrownBy(() -> reservationService.completeSession(command, user))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(SESSION_IS_ALREADY_END);
        }
    }

    @Nested
    @DisplayName("예약 변경 요청 Service TEST")
    class ChangeReservationServiceTest {

        @Test
        @DisplayName("멤버의 예약 변경 요청 - 성공")
        void changeReservationWithMember() {
            //given
            LocalDateTime originDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationCommand.ChangeReqeust command = ReservationCommand.ChangeReqeust.builder()
                    .reservationId(1L)
                    .reservationDate(originDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(originDate))
                    .status(RESERVATION_APPROVED)
                    .build();

            Reservation compltedReservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(changeRequestDate))
                    .status(RESERVATION_CHANGE_REQUEST)
                    .build();

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            when(reservationRepository.saveReservation(reservation))
                    .thenReturn(Optional.ofNullable(compltedReservation));

            //when
            Reservation result = reservationService.changeRequestReservation(command);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getReservationId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(RESERVATION_CHANGE_REQUEST);
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 - 실패: 예약 변경 요청할 수 있는 상태가 아님")
        void changeReservationWithMemberNotAllowChangeRequestStatus() {
            //given
            LocalDateTime originDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationCommand.ChangeReqeust command = ReservationCommand.ChangeReqeust.builder()
                    .reservationId(1L)
                    .reservationDate(originDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(originDate.plusHours(2)))
                    .status(RESERVATION_REFUSED)
                    .build();

            PersonalDetail personalDetail = PersonalDetail.builder()
                    .personalDetailId(1L)
                    .name("트레이너1")
                    .memberId(null)
                    .trainerId(1L)
                    .build();

            SecurityUser user = new SecurityUser(personalDetail);

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            //when & then
            assertThatThrownBy(() -> reservationService.changeFixedReservation(command))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(RESERVATION_CHANGE_REQUEST_NOT_ALLOWED);
        }

        @Test
        @DisplayName("멤버의 예약 변경 요청 - 실패: 요청 예약 변경 날짜가 실제 예약 날짜랑 다름")
        void changeReservationWithMemberNotEqualReservationDate() {
            //given
            LocalDateTime originDate = LocalDateTime.now().plusDays(1);
            LocalDateTime changeRequestDate = LocalDateTime.now().plusDays(2);

            ReservationCommand.ChangeReqeust command = ReservationCommand.ChangeReqeust.builder()
                    .reservationId(1L)
                    .reservationDate(originDate)
                    .changeRequestDate(changeRequestDate)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(originDate.plusHours(2)))
                    .status(RESERVATION_APPROVED)
                    .build();

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            //when & then
            assertThatThrownBy(() -> reservationService.changeRequestReservation(command))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(RESERVATION_DATE_NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("예약 변경 승인 Service TEST")
    class ChangeApproveReservationServiceTest {

        @Test
        @DisplayName("예약 변경 승인 성공")
        void changeApproveReservation() {
            //given
            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationCommand.ChangeApproval command = ReservationCommand.ChangeApproval.builder()
                    .reservationId(1L)
                    .memberId(1L)
                    .isApprove(true)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_CHANGE_REQUEST)
                    .build();

            Reservation compltedReservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_APPROVED)
                    .build();

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            when(reservationRepository.saveReservation(reservation))
                    .thenReturn(Optional.ofNullable(compltedReservation));

            //when
            Reservation result = reservationService.changeApproveReservation(command);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getReservationId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(RESERVATION_APPROVED);
        }

        @Test
        @DisplayName("예약 변경 승인 실패 - 다른 멤버의 예약 변경 수정 시도")
        void changeApproveReservationTryOtherMemberId() {
            //given
            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationCommand.ChangeApproval command = ReservationCommand.ChangeApproval.builder()
                    .reservationId(1L)
                    .memberId(2L)
                    .isApprove(true)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_CHANGE_REQUEST)
                    .build();

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            //when & then
            assertThatThrownBy(() -> reservationService.changeApproveReservation(command))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("예약 변경 승인 실패 - 예약 변경 요청 상태가 아님")
        void changeApproveReservationNotChangeRequestStatus() {
            //given
            LocalDateTime reservationDate = LocalDateTime.now().plusDays(1);

            ReservationCommand.ChangeApproval command = ReservationCommand.ChangeApproval.builder()
                    .reservationId(1L)
                    .memberId(1L)
                    .isApprove(true)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .reservationDates(List.of(reservationDate))
                    .status(RESERVATION_APPROVED)
                    .build();


            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            //when & then
            assertThatThrownBy(() -> reservationService.changeApproveReservation(command))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(RESERVATION_APPROVE_NOT_ALLOWED);
        }
    }

    @Nested
    @DisplayName("예약 취소 승인 Service TEST")
    class CancelApproveReservationServiceTest {

        @Test
        @DisplayName("예약 취소 승인 성공")
        void cancelApproveReservation() {
            //given
            ReservationCommand.CancelApproval command = ReservationCommand.CancelApproval.builder()
                    .reservationId(1L)
                    .memberId(1L)
                    .isApprove(true)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_CANCEL_REQUEST)
                    .build();

            Reservation cancelledReservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_CANCELLED)
                    .build();

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            when(reservationRepository.saveReservation(reservation))
                    .thenReturn(Optional.ofNullable(cancelledReservation));

            //when
            Reservation result = reservationService.cancelApproveReservation(command);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getReservationId()).isEqualTo(1L);
            assertThat(result.getStatus()).isEqualTo(RESERVATION_CANCELLED);
        }

        @Test
        @DisplayName("예약 취소 승인 실패 - 다른 멤버의 예약 취소 요청 수정 시도")
        void cancelApproveReservationTryOtherMemberId() {
            //given
            ReservationCommand.CancelApproval command = ReservationCommand.CancelApproval.builder()
                    .reservationId(1L)
                    .memberId(2L)
                    .isApprove(true)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_CANCEL_REQUEST)
                    .build();

            Reservation cancelledReservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_CANCELLED)
                    .build();

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            when(reservationRepository.saveReservation(reservation))
                    .thenReturn(Optional.ofNullable(cancelledReservation));

            //when & then
            assertThatThrownBy(() -> reservationService.cancelApproveReservation(command))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(MEMBER_NOT_FOUND);
        }

        @Test
        @DisplayName("예약 취소 승인 실패 - 예약 변경 요청 상태가 아님")
        void cancelApproveReservationNotChangeRequestStatus() {
            //given
            ReservationCommand.CancelApproval command = ReservationCommand.CancelApproval.builder()
                    .reservationId(1L)
                    .memberId(1L)
                    .isApprove(true)
                    .build();

            Reservation reservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_APPROVED)
                    .build();

            Reservation cancelledReservation = Reservation.builder()
                    .reservationId(1L)
                    .trainer(Trainer.builder().trainerId(1L).build())
                    .member(Member.builder().memberId(1L).build())
                    .status(RESERVATION_CANCELLED)
                    .build();

            when(reservationRepository.getReservation(command.reservationId()))
                    .thenReturn(Optional.ofNullable(reservation));

            when(reservationRepository.saveReservation(reservation))
                    .thenReturn(Optional.ofNullable(cancelledReservation));


            //when & then
            assertThatThrownBy(() -> reservationService.cancelApproveReservation(command))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(RESERVATION_APPROVE_NOT_ALLOWED);
        }
    }
}