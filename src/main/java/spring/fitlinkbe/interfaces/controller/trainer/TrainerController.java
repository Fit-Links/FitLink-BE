package spring.fitlinkbe.interfaces.controller.trainer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.trainer.TrainerFacade;
import spring.fitlinkbe.application.trainer.criteria.AvailableTimesResult;
import spring.fitlinkbe.application.trainer.criteria.DayOffResult;
import spring.fitlinkbe.application.trainer.criteria.TrainerInfoResult;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.trainer.dto.*;
import spring.fitlinkbe.support.aop.RoleCheck;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RoleCheck(allowedRoles = {UserRole.TRAINER})
@RequestMapping("/v1/trainers")
public class TrainerController {
    private final TrainerFacade trainerFacade;

    @GetMapping("/me")
    public TrainerInfoDto.Response getTrainerInfo(@Login SecurityUser user) {
        TrainerInfoResult.Response response = trainerFacade.getTrainerInfo(user.getTrainerId());

        return TrainerInfoDto.Response.from(response);
    }

    @PatchMapping("/me")
    public TrainerInfoDto.TrainerUpdateResponse updateTrainerInfo(
            @Login SecurityUser user,
            @Valid @RequestBody TrainerInfoDto.TrainerUpdateRequest request) {
        TrainerInfoResult.Response response = trainerFacade.updateTrainerInfo(user.getTrainerId(), request.toRequest());

        return TrainerInfoDto.TrainerUpdateResponse.from(response);
    }

    @GetMapping("/me/trainer-code")
    public TrainerInfoDto.TrainerCodeResponse getTrainerCode(@Login SecurityUser user) {
        TrainerInfoResult.TrainerCodeResponse response = trainerFacade.getTrainerCode(user.getTrainerId());

        return TrainerInfoDto.TrainerCodeResponse.from(response);
    }

    @GetMapping("/me/available-times")
    public AvailableTimesDto.Response getAvailableTimes(@Login SecurityUser user) {
        AvailableTimesResult.Response response = trainerFacade.getAvailableTimes(user.getTrainerId());

        return AvailableTimesDto.Response.from(response);
    }

    @GetMapping("/{trainerId}/available-times")
    @RoleCheck(allowedRoles = {UserRole.MEMBER})
    public ApiResultResponse<AvailableTimesDto.CurrentAvailableTimesResponse> getCurrentAvailableTimes(
            @Login SecurityUser user,
            @PathVariable Long trainerId
    ) {
        AvailableTimesResult.CurrentAvailableTimesResponse response = trainerFacade.getCurrentAvailableTimes(user.getMemberId(), trainerId);

        return ApiResultResponse.of(HttpStatus.OK, true,
                AvailableTimesDto.CurrentAvailableTimesResponse.from(response));
    }

    @PostMapping("/me/available-times")
    public ApiResultResponse<Object> saveAvailableTimes(
            @Login SecurityUser user,
            @Valid @RequestBody AvailableTimesDto.AddRequest request
    ) {
        trainerFacade.saveAvailableTimes(user.getTrainerId(), request.toCriteria());

        return ApiResultResponse.of(HttpStatus.CREATED, true, null);
    }


    @DeleteMapping("/me/available-times")
    public ApiResultResponse<Object> deleteAvailableTimes(
            @Login SecurityUser user,
            @RequestParam LocalDate applyAt
    ) {
        trainerFacade.deleteAvailableTimes(user.getTrainerId(), applyAt);

        return ApiResultResponse.of(HttpStatus.NO_CONTENT, true, null);
    }

    @PostMapping("/me/day-off")
    public ApiResultResponse<List<DayOffDto.Response>> saveDayOff(
            @Login SecurityUser user,
            @Valid @RequestBody List<LocalDate> request
    ) {
        List<DayOffResult.Response> result = trainerFacade.saveDayOff(user.getTrainerId(), request);

        return ApiResultResponse.of(HttpStatus.CREATED, true,
                result.stream().map(DayOffDto.Response::from).toList());
    }

    @DeleteMapping("/me/day-off/{dayOffId}")
    public ApiResultResponse<Object> deleteDayOff(
            @Login SecurityUser user,
            @PathVariable Long dayOffId
    ) {
        trainerFacade.deleteDayOff(user.getTrainerId(), dayOffId);

        return ApiResultResponse.of(HttpStatus.NO_CONTENT, true, null);
    }

    @GetMapping("/me/day-off")
    public ApiResultResponse<List<DayOffDto.Response>> getDayOff(@Login SecurityUser user) {
        List<DayOffResult.Response> result = trainerFacade.getDayOff(user.getTrainerId());

        return ApiResultResponse.of(HttpStatus.OK, true,
                result.stream().map(DayOffDto.Response::from).toList());
    }

    @PostMapping("/disconnect")
    public ApiResultResponse<Object> disconnectTrainer(
            @Login SecurityUser user,
            @RequestBody @Valid TrainerDto.MemberDisconnectRequest request
    ) {
        trainerFacade.disconnectTrainer(user.getTrainerId(), request.memberId());

        return ApiResultResponse.of(HttpStatus.NO_CONTENT, true, null);
    }

    @PostMapping("/connect-requests/{notificationId}/decision")
    public ApiResultResponse<Object> decisionConnectRequest(
            @Login SecurityUser user,
            @PathVariable Long notificationId,
            @RequestBody @Valid ConnectRequestDecisionDto request
    ) {
        trainerFacade.decisionConnectRequest(user.getTrainerId(), notificationId, request.isApproved());

        return ApiResultResponse.of(HttpStatus.NO_CONTENT, true, null);
    }
}
