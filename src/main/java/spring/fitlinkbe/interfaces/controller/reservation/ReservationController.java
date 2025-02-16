package spring.fitlinkbe.interfaces.controller.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.reservation.ReservationFacade;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationRequestDto;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationResponseDto;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
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

        return ApiResultResponse.ok(reservationFacade.getReservations(date, user).reservations().stream()
                .map(ReservationResponseDto.GetList::of).toList());

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
     * 예약 불가 설정
     *
     * @param request 예약 불가 설정할 date 정보
     * @return ApiResultResponse 예약 불가 설정된 reservationId 정보를 반환한다.
     */
    @PostMapping("/availability/disable")
    public ApiResultResponse<ReservationResponseDto.Success> setDisabledTime(@RequestBody @Valid
                                                                             ReservationRequestDto.SetDisabledTime
                                                                                     request,
                                                                             @Login SecurityUser user) {

        return ApiResultResponse.ok(ReservationResponseDto.Success.of(
                reservationFacade.setDisabledReservation(request.toCriteria(user.getTrainerId()))));
    }
}