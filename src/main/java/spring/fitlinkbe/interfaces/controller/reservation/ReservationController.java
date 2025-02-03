package spring.fitlinkbe.interfaces.controller.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.application.reservation.ReservationFacade;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.reservation.dto.ReservationDto;
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

    @GetMapping
    public ApiResultResponse<List<ReservationDto.Response>> getReservations(@RequestParam LocalDate date,
                                                                            @Login SecurityUser user) {

        return ApiResultResponse.ok(reservationFacade.getReservations(date, user).stream()
                .map(ReservationDto.Response::of).toList());

    }
}