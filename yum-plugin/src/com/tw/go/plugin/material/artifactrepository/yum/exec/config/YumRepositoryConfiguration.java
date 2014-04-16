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

package com.tw.go.plugin.material.artifactrepository.yum.exec.config;

import com.thoughtworks.go.plugin.api.config.Configuration;
import com.thoughtworks.go.plugin.api.config.Property;
import static com.thoughtworks.go.plugin.api.config.Property.*;
import com.thoughtworks.go.plugin.api.material.packagerepository.*;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.yum.exec.Constants;
import com.tw.go.plugin.material.artifactrepository.yum.exec.RepoUrl;
import com.tw.go.plugin.material.artifactrepository.yum.exec.YumRepositoryPoller;
import com.tw.go.plugin.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;

public class YumRepositoryConfiguration implements PackageMaterialConfiguration {

    public RepositoryConfiguration getRepositoryConfiguration() {
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
        repositoryConfiguration.add(new PackageMaterialProperty(Constants.REPO_URL).with(DISPLAY_NAME, "Repository URL").with(DISPLAY_ORDER, 0));
        repositoryConfiguration.add(new PackageMaterialProperty(Constants.USERNAME).with(REQUIRED, false).with(PART_OF_IDENTITY, false).with(DISPLAY_NAME, "User").with(DISPLAY_ORDER, 1));
        repositoryConfiguration.add(new PackageMaterialProperty(Constants.PASSWORD).with(REQUIRED, false).with(PART_OF_IDENTITY, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2));
        return repositoryConfiguration;
    }

    public PackageConfiguration getPackageConfiguration() {
        PackageConfiguration packageConfiguration = new PackageConfiguration();
        packageConfiguration.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC).with(DISPLAY_NAME, "Package Spec").with(DISPLAY_ORDER, 0));
        return packageConfiguration;
    }

    public ValidationResult isRepositoryConfigurationValid(RepositoryConfiguration repositoryConfiguration) {
        ValidationResult validationResult = new ValidationResult();
        validateKeys(getRepositoryConfiguration(), repositoryConfiguration, validationResult);

        Property repositoryUrl = repositoryConfiguration.get(Constants.REPO_URL);
        Property username = repositoryConfiguration.get(Constants.USERNAME);
        Property password = repositoryConfiguration.get(Constants.PASSWORD);

        if (repositoryUrl == null) {
            validationResult.addError(new ValidationError(Constants.REPO_URL, "Repository url not specified"));
            return validationResult;
        }
        String usernameValue = username == null ? null : username.getValue();
        String passwordValue = password == null ? null : password.getValue();

        new RepoUrl(repositoryUrl.getValue(), usernameValue, passwordValue).validate(validationResult);
        return validationResult;
    }

    public ValidationResult isPackageConfigurationValid(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration) {
        ValidationResult validationResult = new ValidationResult();
        validateKeys(getPackageConfiguration(), packageConfiguration, validationResult);
        Property artifactIdConfiguration = packageConfiguration.get(Constants.PACKAGE_SPEC);
        if (artifactIdConfiguration == null) {
            validationResult.addError(new ValidationError(Constants.PACKAGE_SPEC, "Package spec not specified"));
            return validationResult;
        }
        String packageSpec = artifactIdConfiguration.getValue();
        if (packageSpec == null) {
            validationResult.addError(new ValidationError(Constants.PACKAGE_SPEC, "Package spec is null"));
            return validationResult;
        }
        if (isBlank(packageSpec.trim())) {
            validationResult.addError(new ValidationError(Constants.PACKAGE_SPEC, "Package spec is empty"));
            return validationResult;
        }
        return validationResult;
    }

    private void validateKeys(Configuration configDefinedByPlugin, Configuration configDefinedByUser, ValidationResult validationResult) {
        List<String> validKeys = new ArrayList<String>();
        List<String> invalidKeys = new ArrayList<String>();
        for (Property configuration : configDefinedByPlugin.list()) {
            validKeys.add(configuration.getKey());
        }

        for (Property configuration : configDefinedByUser.list()) {
            if (!validKeys.contains(configuration.getKey())) {
                invalidKeys.add(configuration.getKey());
            }
        }
        if (!invalidKeys.isEmpty()) {
            validationResult.addError(new ValidationError("", String.format("Unsupported key(s) found : %s. Allowed key(s) are : %s", ListUtil.join(invalidKeys), ListUtil.join(validKeys))));
        }
    }

    public void validate(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration, ValidationResult validationResult) {
        ValidationResult repositoryConfigurationValidationResult = isRepositoryConfigurationValid(repositoryConfiguration);
        validationResult.addErrors(repositoryConfigurationValidationResult.getErrors());
        ValidationResult packageConfigurationValidationResult = isPackageConfigurationValid(packageConfiguration, repositoryConfiguration);
        validationResult.addErrors(packageConfigurationValidationResult.getErrors());
    }

    public void testConnection(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration) {
        try {
            new YumRepositoryPoller().getLatestRevision(packageConfiguration, repositoryConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Test Connection failed.", e);
        }
    }
}
