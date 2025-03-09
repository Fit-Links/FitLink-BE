package spring.fitlinkbe.support.aop;

import spring.fitlinkbe.domain.common.enums.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RoleCheck {
    UserRole[] allowedRoles(); // 허용할 역할 ("TRAINER", "MEMBER")
}
