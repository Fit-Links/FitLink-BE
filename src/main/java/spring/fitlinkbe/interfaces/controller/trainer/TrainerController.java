package spring.fitlinkbe.interfaces.controller.trainer;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.trainer.dto.TrainerDto;
import spring.fitlinkbe.support.aop.RoleCheck;

@RestController
@RequiredArgsConstructor
@RoleCheck(allowedRoles = {"TRAINER"})
@RequestMapping("/v1/trainers")
public class TrainerController {
    private final TrainerService trainerService;

    @GetMapping("{trainerId}/me")
    public ApiResultResponse<TrainerDto.Response> getTrainerInfo(@PathVariable("trainerId")
                                                                 @NotNull(message = "유저 ID는 필수값 입니다.")
                                                                 Long trainerId) {

        return ApiResultResponse.ok(TrainerDto.Response.of(trainerService.getTrainerInfo(trainerId)));

    }

}
