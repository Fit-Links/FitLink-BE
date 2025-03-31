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
import spring.fitlinkbe.interfaces.controller.trainer.dto.AvailableTimesDto;
import spring.fitlinkbe.interfaces.controller.trainer.dto.DayOffDto;
import spring.fitlinkbe.interfaces.controller.trainer.dto.TrainerInfoDto;
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

<<<<<<< HEAD
=======
    @DeleteMapping("/me/day-off/{dayOffId}")
    public ApiResultResponse<Object> deleteDayOff(
            @Login SecurityUser user,
            @PathVariable Long dayOffId
    ) {
        trainerFacade.deleteDayOff(user.getTrainerId(), dayOffId);

        return ApiResultResponse.of(HttpStatus.NO_CONTENT, true, null);
    }

>>>>>>> 2168115 (:sparkles: "휴무일 삭제 api 작업")
}
