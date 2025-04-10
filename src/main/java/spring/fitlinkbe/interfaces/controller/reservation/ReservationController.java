package spring.fitlinkbe.interfaces.controller.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.reservation.ReservationFacade;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Reservation;
import spring.fitlinkbe.domain.reservation.Session;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationRequestDto;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationResponseDto;
import spring.fitlinkbe.support.aop.RoleCheck;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RoleCheck(allowedRoles = {UserRole.TRAINER, UserRole.MEMBER})
@RequestMapping("/v1/reservations")
@Slf4j
public class ReservationController {

    private final ReservationFacade reservationFacade;

    /**
     * 예약 목록 조회
     *
     * @param date 예약 정보를 받고 싶은 date 정보
     * @param user 인증된 유저 정보
     * @return ApiResultResponse 예약 목록을 반환한다.
     */
    @GetMapping
    public ApiResultResponse<List<ReservationResponseDto.Summary>> getReservations(@RequestParam LocalDate date,
                                                                                   @Login SecurityUser user) {

        List<Reservation> result = reservationFacade.getReservations(date, user);

        return ApiResultResponse.ok(result.stream()
                .map(ReservationResponseDto.Summary::of)
                .toList());
    }

    /**
     * 예약 상세 조회
     *
     * @param reservationId reservationId
     * @return ApiResultResponse 예약 상세 정보를 반환한다.
     */

    @GetMapping("/{reservationId}")
    public ApiResultResponse<ReservationResponseDto.Detail> getReservationDetail(@PathVariable("reservationId")
                                                                                 @NotNull Long reservationId) {

        return ApiResultResponse.ok(ReservationResponseDto.Detail.of(
                reservationFacade.getReservationDetail(reservationId)));

    }

    /**
     * 예약 상세 대기 조회
     *
     * @param reservationDate reservationDate 정보
     * @return ApiResultResponse 예약 상세 대기 멤버 정보를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @GetMapping("/waiting-members/{reservationDate}")
    public ApiResultResponse<List<ReservationResponseDto.WaitingMember>> getWaitingMembers(@PathVariable("reservationDate")
                                                                                           @NotNull(message = "예약 날짜는 필수입니다.")
                                                                                           LocalDateTime reservationDate,
                                                                                           @Login SecurityUser user) {
        List<Reservation> result = reservationFacade.getWaitingMembers(reservationDate, user);


        return ApiResultResponse.ok(result.stream()
                .map(ReservationResponseDto.WaitingMember::of)
                .toList());
    }

    /**
     * 예약 불가 설정
     *
     * @param request 예약 불가 설정할 date 정보
     * @return ApiResultResponse 예약 불가 설정된 reservationId 정보를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @PostMapping("/availability/disable")
    public ApiResultResponse<ReservationResponseDto.Success> setDisabledTime(@RequestBody @Valid
                                                                             ReservationRequestDto.SetDisabledTime
                                                                                     request,
                                                                             @Login SecurityUser user) {

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(
                reservationFacade.setDisabledReservation(request.toCriteria(), user)));
    }

    /**
     * 고정 예약
     *
     * @param request memberId, name, dates 정보
     * @return ApiResultResponse 고정 예약이 된 reservationId 목록 정보를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @PostMapping("/fixed-reservations")
    public ApiResultResponse<List<ReservationResponseDto.Success>> createFixedReservation(@RequestBody @Valid
                                                                                          ReservationRequestDto.CreateFixed
                                                                                                  request,
                                                                                          @Login SecurityUser user) {

        List<Reservation> result = reservationFacade.createFixedReservation(request.toCriteria(), user);

        return ApiResultResponse.ok(result.stream()
                .map(ReservationResponseDto.Success::of)
                .toList());

    }

    /**
     * 직접 예약
     *
     * @param request memberId, name, dates 정보
     * @return ApiResultResponse 예약이 된 reservationId 목록 정보를 반환한다.
     */
    @PostMapping
    public ApiResultResponse<ReservationResponseDto.Success> createReservation(@RequestBody @Valid
                                                                               ReservationRequestDto.Create
                                                                                       request,
                                                                               @Login SecurityUser user
    ) {

        Reservation result = reservationFacade.createReservation(request.toCriteria(), user);

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));

    }

    /**
     * 예약 승인
     *
     * @param request memberId 정보
     * @return ApiResultResponse 승인된 reservationId 정보를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @PostMapping("/{reservationId}/approve")
    public ApiResultResponse<ReservationResponseDto.Success> approveReservation(@PathVariable("reservationId")
                                                                                @NotNull(message = "예약 ID는 필수값입니다.")
                                                                                Long reservationId,
                                                                                @RequestBody @Valid
                                                                                ReservationRequestDto.Approve
                                                                                        request,
                                                                                @Login SecurityUser user
    ) {
        Reservation result = reservationFacade.approveReservation(request.toCriteria(reservationId), user);

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));

    }


    /**
     * 예약 취소
     *
     * @param request cancelReason 정보
     * @return ApiResultResponse 취소된 reservationId 정보를 반환한다.
     */
    @PostMapping("/{reservationId}/cancel")
    public ApiResultResponse<ReservationResponseDto.Success> cancelReservation(@PathVariable("reservationId")
                                                                               @NotNull(message = "예약 ID는 필수값입니다.")
                                                                               Long reservationId,
                                                                               @RequestBody @Valid
                                                                               ReservationRequestDto.Cancel
                                                                                       request,
                                                                               @Login SecurityUser user
    ) {

        Reservation result = reservationFacade.cancelReservation(request.toCriteria(reservationId), user);

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));

    }

    /**
     * 예약 취소 승인
     *
     * @param request cancelReason 정보
     * @return ApiResultResponse 취소된 reservationId 정보를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @PostMapping("/{reservationId}/cancel-approve")
    public ApiResultResponse<ReservationResponseDto.Success> cancelApproveReservation(@PathVariable("reservationId")
                                                                                      @NotNull(message = "예약 ID는 필수값입니다.")
                                                                                      Long reservationId,
                                                                                      @RequestBody @Valid
                                                                                      ReservationRequestDto.CancelApproval
                                                                                              request,
                                                                                      @Login SecurityUser user

    ) {

        Reservation result = reservationFacade.cancelApproveReservation(request.toCriteria(reservationId), user);

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));

    }

    /**
     * 예약 변경
     * 트레이너 - 고정 예약 변경, 멤버 - 예약 변경 요청
     *
     * @param request reservationDate, changeDate 정보
     * @return ApiResultResponse 변경 요청이 된 reservationId 결과를 반환한다.
     */
    @PostMapping("{reservationId}/change")
    public ApiResultResponse<ReservationResponseDto.Success> changeReservation(@PathVariable("reservationId")
                                                                               @NotNull(message = "예약 ID는 필수값입니다.")
                                                                               Long reservationId,
                                                                               @RequestBody @Valid
                                                                               ReservationRequestDto.ChangeReqeust
                                                                                       request,
                                                                               @Login SecurityUser user) {
        Reservation result = reservationFacade.changeReservation(request.toCriteria(reservationId), user);

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));
    }

    /**
     * 예약 변경 요청 승인
     *
     * @param request // memberId 정보
     * @return ApiResultResponse 변경 승인 된 reservationId 결과를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @PostMapping("{reservationId}/change-approve")
    public ApiResultResponse<ReservationResponseDto.Success> changeApproveReservation(@PathVariable("reservationId")
                                                                                      @NotNull(message = "예약 ID는 필수값입니다.")
                                                                                      Long reservationId,
                                                                                      @RequestBody @Valid
                                                                                      ReservationRequestDto.ChangeApproval
                                                                                              request) {

        Reservation result = reservationFacade.changeApproveReservation(request.toCriteria(reservationId));

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));
    }

    /**
     * 진행한 PT 처리
     *
     * @param request isJoin 정보
     * @return ApiResultResponse 진행한 sessionId 결과를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @PostMapping("{reservationId}/sessions/complete")
    public ApiResultResponse<ReservationResponseDto.SuccessSession> completeSession(@PathVariable("reservationId")
                                                                                    @NotNull(message = "예약 ID는 필수값입니다.")
                                                                                    Long reservationId,
                                                                                    @RequestBody @Valid
                                                                                    ReservationRequestDto.Complete
                                                                                            request,
                                                                                    @Login SecurityUser user
    ) {
        Session result = reservationFacade.completeSession(request.toCriteria(reservationId), user);

        return ApiResultResponse.ok(ReservationResponseDto.SuccessSession.of(result));

    }
}