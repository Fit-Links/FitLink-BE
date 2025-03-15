package spring.fitlinkbe.interfaces.controller.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.application.trainer.TrainerFacade;
import spring.fitlinkbe.application.trainer.criteria.TrainerInfoResult;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.trainer.TrainerService;
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
}
