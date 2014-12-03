package com.tw.go.plugin.material.artifactrepository.yum.exec.message;

import java.util.ArrayList;
import java.util.List;

public class ValidationResultMessage {

    private List<ValidationError> validationErrors = new ArrayList<ValidationError>();

    public void addError(ValidationError validationError) {
        validationErrors.add(validationError);
    }

    public boolean failure() {
        return !validationErrors.isEmpty();
    }

    public List<String> getMessages() {
        List<String> errorMessages = new ArrayList<String>();
        for (ValidationError error : validationErrors) {
            errorMessages.add(error.getMessage());
        }
        return errorMessages;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public Boolean success() {
        return !failure();
    }
}
