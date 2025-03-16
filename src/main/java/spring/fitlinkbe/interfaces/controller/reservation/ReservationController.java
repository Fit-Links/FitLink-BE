package spring.fitlinkbe.interfaces.controller.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.reservation.ReservationFacade;
import spring.fitlinkbe.application.reservation.criteria.ReservationResult;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.reservation.Reservation;
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
    public ApiResultResponse<List<ReservationResponseDto.GetList>> getReservations(@RequestParam LocalDate date,
                                                                                   @Login SecurityUser user) {

        List<Reservation> result = reservationFacade.getReservations(date, user).reservations();

        return ApiResultResponse.ok(result.stream()
                .map(ReservationResponseDto.GetList::of)
                .toList());
    }

    /**
     * 예약 상세 조회
     *
     * @param reservationId reservationId
     * @return ApiResultResponse 예약 상세 정보를 반환한다.
     */

    @GetMapping("/{reservationId}")
    public ApiResultResponse<ReservationResponseDto.GetDetail> getReservation(@PathVariable("reservationId")
                                                                              @NotNull Long reservationId) {

        return ApiResultResponse.ok(ReservationResponseDto.GetDetail.of(
                reservationFacade.getReservation(reservationId)));

    }

    /**
     * 예약 상세 대기 조회
     *
     * @param reservationDate reservationDate 정보
     * @return ApiResultResponse 예약 상세 대기 멤버 정보를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @GetMapping("/waiting-members/{reservationDate}")
    public ApiResultResponse<List<ReservationResponseDto.GetWaitingMember>> getWaitingMembers(@PathVariable("reservationDate")
                                                                                              @NotNull(message = "예약 날짜는 필수입니다.")
                                                                                              LocalDateTime reservationDate,
                                                                                              @Login SecurityUser user) {


        List<ReservationResult.ReservationWaitingMember> result = reservationFacade.getWaitingMembers(reservationDate
                , user);

        return ApiResultResponse.ok(result.stream()
                .map(ReservationResponseDto.GetWaitingMember::of)
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
     * 직접 예약
     *
     * @param request memberId, name, dates 정보
     * @return ApiResultResponse 예약이 된 reservationId 목록 정보를 반환한다.
     */
    @PostMapping
    public ApiResultResponse<ReservationResponseDto.Success> reserveSession(@RequestBody @Valid
                                                                            ReservationRequestDto.ReserveSession
                                                                                    request,
                                                                            @Login SecurityUser user
    ) {

        Reservation result = reservationFacade.reserveSession(request.toCriteria(), user);

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));

    }

    /**
     * 고정 예약
     *
     * @param request memberId, name, dates 정보
     * @return ApiResultResponse 고정 예약이 된 reservationId 목록 정보를 반환한다.
     */
    @RoleCheck(allowedRoles = {UserRole.TRAINER})
    @PostMapping("/fixed-reservations")
    public ApiResultResponse<List<ReservationResponseDto.Success>> fixedReserveSession(@RequestBody @Valid
                                                                                       ReservationRequestDto.FixedReserveSession
                                                                                               request,
                                                                                       @Login SecurityUser user) {

        ReservationResult.Reservations result = reservationFacade.fixedReserveSession(request.toCriteria(), user);

        return ApiResultResponse.ok(result.reservations().stream()
                .map(ReservationResponseDto.Success::of)
                .toList());

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
                                                                               ReservationRequestDto.CancelReservation
                                                                                       request,
                                                                               @Login SecurityUser user
    ) {

        Reservation result = reservationFacade.cancelReservation(request.toCriteria(reservationId), user);

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
                                                                                ReservationRequestDto.ApproveReservation
                                                                                        request,
                                                                                @Login SecurityUser user
    ) {
        Reservation result = reservationFacade.approveReservation(request.toCriteria(reservationId), user);

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(result));

    }
}