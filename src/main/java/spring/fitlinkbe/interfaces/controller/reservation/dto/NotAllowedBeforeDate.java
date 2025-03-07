package spring.fitlinkbe.interfaces.controller.reservation.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotAllowedBeforeDateValidator.class)
public @interface NotAllowedBeforeDate {

    String message() default "현재 날짜보다 이전 날짜는 설정이 불가능 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
