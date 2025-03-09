package spring.fitlinkbe.support.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import spring.fitlinkbe.domain.common.enums.UserRole;
import spring.fitlinkbe.support.security.SecurityUser;

import javax.security.sasl.AuthenticationException;
import java.util.List;

@Aspect
@Component
public class RoleCheckAspect {

    @Around("@annotation(roleCheck)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RoleCheck roleCheck) throws Throwable {
        // 현재 요청의 SecurityContext에서 JWT 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("인증 정보가 없습니다.");
        }

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        // 유저 역할(Role) 가져오기
        UserRole userRole = securityUser.getUserRole();

        // 요청된 API에서 허용하는 역할 목록
        List<UserRole> allowedRoles = List.of(roleCheck.allowedRoles());

        if (!allowedRoles.contains(userRole)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        return joinPoint.proceed();
    }

}
