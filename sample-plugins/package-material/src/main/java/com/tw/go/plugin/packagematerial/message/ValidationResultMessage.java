/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/
package com.tw.go.plugin.packagematerial.message;

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
