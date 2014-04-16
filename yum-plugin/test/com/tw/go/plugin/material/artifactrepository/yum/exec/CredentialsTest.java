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

package com.tw.go.plugin.material.artifactrepository.yum.exec;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class CredentialsTest {
    @Test
    public void shouldGetUserInfo() throws Exception {
        Credentials credentials = new Credentials("user", "password");
        assertThat(credentials.getUserInfo(), is("user:password"));
    }

    @Test
    public void shouldGetUserInfoWithEscapedPassword() throws Exception {
        Credentials credentials = new Credentials("user", "!password@:");
        assertThat(credentials.getUserInfo(), is("user:%21password%40%3A"));
    }

    @Test
    public void shouldFailValidationIfOnlyPasswordProvided() throws Exception {
        ValidationResult validationResult = new ValidationResult();
        new Credentials(null, "password").validate(validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors(), hasItem(new ValidationError(Constants.USERNAME, "Both Username and password are required.")));

        validationResult = new ValidationResult();
        new Credentials("user", "").validate(validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors(), hasItem(new ValidationError(Constants.PASSWORD, "Both Username and password are required.")));
    }
}
