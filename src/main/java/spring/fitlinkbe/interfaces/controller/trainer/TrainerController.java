package spring.fitlinkbe.interfaces.controller.trainer;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.trainer.TrainerService;
import spring.fitlinkbe.support.aop.RoleCheck;

@RestController
@RequiredArgsConstructor
@RoleCheck(allowedRoles = {UserRole.TRAINER})
@RequestMapping("/v1/trainers")
public class TrainerController {
    private final TrainerService trainerService;


}
