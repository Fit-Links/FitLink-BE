package spring.fitlinkbe.interfaces.controller.reservation.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.util.List;

public class NotAllowedBeforeDateValidator implements ConstraintValidator<NotAllowedBeforeDate, List<LocalDateTime>> {

    @Override
    public void initialize(NotAllowedBeforeDate constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<LocalDateTime> dates, ConstraintValidatorContext context) {
        if (dates == null || dates.isEmpty()) {
            return true;
        }
        LocalDateTime nowDate = LocalDateTime.now();
        return dates.stream().noneMatch(date -> date.isBefore(nowDate));
    }
}