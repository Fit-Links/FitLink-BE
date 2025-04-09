package spring.fitlinkbe.interfaces.controller.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring.fitlinkbe.application.notification.NotificationFacade;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.common.dto.CustomPageResponse;
import spring.fitlinkbe.interfaces.controller.notification.dto.NotificationResponseDto;
import spring.fitlinkbe.support.aop.RoleCheck;
import spring.fitlinkbe.support.argumentresolver.Login;
import spring.fitlinkbe.support.security.SecurityUser;

@RestController
@RequiredArgsConstructor
@RoleCheck(allowedRoles = {UserRole.TRAINER, UserRole.MEMBER})
@RequestMapping("/v1/notifications")
@Slf4j
public class NotificationController {

    private final NotificationFacade notificationFacade;

    /**
     * 알림 조회
     *
     * @param type        알림 조회 타입
     * @param pageRequest 조회하고 싶은 페이지 옵션(page, size)
     * @param user        인증된 유저 정보
     * @return ApiResultResponse 알림 목록을 반환한다.
     */
    @GetMapping
    public ApiResultResponse<CustomPageResponse<NotificationResponseDto.Summary>> getNotifications(
            @RequestParam(required = false) Notification.ReferenceType type,
            @PageableDefault Pageable pageRequest,
            @Login SecurityUser user) {

        Page<Notification> result = notificationFacade.getNotifications(type, pageRequest, user);

        return ApiResultResponse.ok(CustomPageResponse.of(result, NotificationResponseDto.Summary::of));

    }


}
