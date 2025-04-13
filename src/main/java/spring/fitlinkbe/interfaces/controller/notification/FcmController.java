package spring.fitlinkbe.interfaces.controller.notification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.application.notification.NotificationFacade;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.notification.dto.FcmRequestDto;
import spring.fitlinkbe.interfaces.controller.notification.dto.FcmResponseDto;
import spring.fitlinkbe.support.aop.RoleCheck;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

@RestController
@RequiredArgsConstructor
@RoleCheck(allowedRoles = {UserRole.TRAINER, UserRole.MEMBER})
@RequestMapping("/v1/fcm")
@Slf4j
public class FcmController {

    private final NotificationFacade notificationFacade;

    /**
     * 클라이언트에서 전달한 FCM Token 정보를 저장합니다.
     *
     * @param request fcmToken 정보
     * @return ApiResultResponse 토큰이 성공적으로 저장됐다는 메시지를 리턴한다.
     */
    @PostMapping("/token")
    public ApiResultResponse<FcmResponseDto.FcmTokenResponse> saveFcmToken(@RequestBody @Valid FcmRequestDto.FcmTokenRequest
                                                                                request,
                                                                        @Login SecurityUser user) {
        notificationFacade.saveFcmToken(request.toCriteria(), user);
        return ApiResultResponse.ok(FcmResponseDto.FcmTokenResponse.of("FCM 토큰 등록에 성공하였습니다."));
    }
}
