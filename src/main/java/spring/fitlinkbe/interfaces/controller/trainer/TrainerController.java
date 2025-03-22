package spring.fitlinkbe.interfaces.controller.trainer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.trainer.TrainerFacade;
import spring.fitlinkbe.application.trainer.criteria.TrainerInfoResult;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.interfaces.controller.trainer.dto.TrainerInfoDto;
import spring.fitlinkbe.support.aop.RoleCheck;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

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
}
