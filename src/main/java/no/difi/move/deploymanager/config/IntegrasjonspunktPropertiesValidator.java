package no.difi.move.deploymanager.config;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class IntegrasjonspunktPropertiesValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {
        return aClass == DeployManagerProperties.IntegrasjonspunktProperties.class;
    }

    @Override
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "profile", DeployManagerPropertiesValidator.NOT_EMPTY_MESSAGE);
    }
}
