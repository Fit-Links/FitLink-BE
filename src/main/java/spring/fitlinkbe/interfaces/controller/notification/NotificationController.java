package spring.fitlinkbe.interfaces.controller.notification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import spring.fitlinkbe.application.notification.NotificationFacade;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.domain.notification.Notification;
import spring.fitlinkbe.interfaces.controller.common.dto.ApiResultResponse;
import spring.fitlinkbe.interfaces.controller.common.dto.CustomPageResponse;
import spring.fitlinkbe.interfaces.controller.notification.dto.NotificationRequestDto;
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
            @RequestParam(required = false) String q,
            @Login SecurityUser user) {

        Page<Notification> result = notificationFacade.getNotifications(type, pageRequest, user, q);

        return ApiResultResponse.ok(CustomPageResponse.of(result, NotificationResponseDto.Summary::of));

    }


    /**
     * 알림 상세 조회
     *
     * @param notificationId 알고 싶은 알림 ID
     * @param user           인증된 유저 정보
     * @return ApiResultResponse 알림 상세 정보를 반환한다.
     */
    @GetMapping("/{notificationId}")
    public ApiResultResponse<NotificationResponseDto.Detail> getNotificationDetail(@PathVariable("notificationId")
                                                                                   Long notificationId,
                                                                                   @Login SecurityUser user) {

        Notification result = notificationFacade.getNotificationDetail(notificationId, user);

        return ApiResultResponse.ok(NotificationResponseDto.Detail.of(result));

    }

    /**
     * push token 등록
     *
     * @param request push-token 정보
     * @return ApiResultResponse 토큰이 성공적으로 저장됐다는 메시지를 리턴한다.
     */
    @PostMapping("/push-token/register")
    public ApiResultResponse<NotificationResponseDto.PushToken> registerPushToken(@RequestBody @Valid
                                                                                  NotificationRequestDto.PushTokenRequest
                                                                                          request,
                                                                                  @Login SecurityUser user) {

        notificationFacade.registerPushToken(request.toCriteria(), user);

        return ApiResultResponse.ok(NotificationResponseDto.PushToken.of("푸쉬 토큰 등록에 성공하였습니다."));
    }
}
