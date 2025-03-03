package spring.fitlinkbe.support.validator;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Collection;

@RequiredArgsConstructor
@Component
public class CollectionValidator implements Validator {

    private final LocalValidatorFactoryBean validator;

    @Override
    public boolean supports(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof Collection collection) {
            collection.forEach(el -> validator.validate(el, errors));
        }
    }
}
