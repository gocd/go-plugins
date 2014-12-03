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

import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageMaterialProperties;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageMaterialProperty;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.ValidationError;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.ValidationResultMessage;

import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.common.util.ListUtil.join;
import static com.tw.go.plugin.common.util.StringUtil.isBlank;
import static com.tw.go.plugin.material.artifactrepository.yum.exec.message.ValidationError.create;

public class PackageRepositoryConfigurationProvider {

    public PackageMaterialProperties repositoryConfiguration() {
        PackageMaterialProperties repositoryConfigurationResponse = new PackageMaterialProperties();
        repositoryConfigurationResponse.addPackageMaterialProperty(Constants.REPO_URL, url());
        repositoryConfigurationResponse.addPackageMaterialProperty(Constants.USERNAME, username());
        repositoryConfigurationResponse.addPackageMaterialProperty(Constants.PASSWORD, password());
        return repositoryConfigurationResponse;
    }

    public PackageMaterialProperties packageConfiguration() {
        PackageMaterialProperties packageConfigurationResponse = new PackageMaterialProperties();
        packageConfigurationResponse.addPackageMaterialProperty(Constants.PACKAGE_SPEC, packageSpec());
        return packageConfigurationResponse;
    }

    public ValidationResultMessage validateRepositoryConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        ValidationResultMessage validationResultMessage = new ValidationResultMessage();
        validateKeys(repositoryConfiguration(), configurationProvidedByUser, validationResultMessage);
        PackageMaterialProperty repositoryUrl = configurationProvidedByUser.getProperty(Constants.REPO_URL);
        PackageMaterialProperty username = configurationProvidedByUser.getProperty(Constants.USERNAME);
        PackageMaterialProperty password = configurationProvidedByUser.getProperty(Constants.PASSWORD);

        if (repositoryUrl == null) {
            validationResultMessage.addError(ValidationError.create(Constants.REPO_URL, "Repository url not specified"));
            return validationResultMessage;
        }
        String usernameValue = username == null ? null : username.value();
        String passwordValue = password == null ? null : password.value();

        new RepoUrl(repositoryUrl.value(), usernameValue, passwordValue).validate(validationResultMessage);

        return validationResultMessage;
    }

    public ValidationResultMessage validatePackageConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        ValidationResultMessage validationResultMessage = new ValidationResultMessage();
        validateKeys(packageConfiguration(), configurationProvidedByUser, validationResultMessage);
        PackageMaterialProperty artifactIdConfiguration = configurationProvidedByUser.getProperty(Constants.PACKAGE_SPEC);
        if (artifactIdConfiguration == null) {
            validationResultMessage.addError(ValidationError.create(Constants.PACKAGE_SPEC, "Package spec not specified"));
            return validationResultMessage;
        }
        String packageSpec = artifactIdConfiguration.value();
        if (packageSpec == null) {
            validationResultMessage.addError(ValidationError.create(Constants.PACKAGE_SPEC, "Package spec is null"));
            return validationResultMessage;
        }
        if (isBlank(packageSpec.trim())) {
            validationResultMessage.addError(ValidationError.create(Constants.PACKAGE_SPEC, "Package spec is empty"));
            return validationResultMessage;
        }
        return validationResultMessage;
    }

    private void validateKeys(PackageMaterialProperties configDefinedByPlugin, PackageMaterialProperties configProvidedByUser, ValidationResultMessage validationResultMessage) {
        List<String> invalidKeys = new ArrayList<String>();

        for (String key : configProvidedByUser.keys()) {
            if (!configDefinedByPlugin.hasKey(key)) {
                invalidKeys.add(key);
            }
        }
        if (!invalidKeys.isEmpty()) {
            validationResultMessage.addError(create(String.format("Unsupported key(s) found : %s. Allowed key(s) are : %s", join(invalidKeys), join(configDefinedByPlugin.keys()))));
        }
    }

    private PackageMaterialProperty packageSpec() {
        return new PackageMaterialProperty().withDisplayName("Package Spec").withDisplayOrder("0");
    }

    private PackageMaterialProperty password() {
        return new PackageMaterialProperty().
                withRequired(false).
                withPartOfIdentity(false).
                withSecure(true).
                withDisplayName("Password").
                withDisplayOrder("2");
    }

    private PackageMaterialProperty username() {
        return new PackageMaterialProperty().
                withRequired(false).
                withPartOfIdentity(false).
                withDisplayName("User").
                withDisplayOrder("1");
    }

    private PackageMaterialProperty url() {
        return new PackageMaterialProperty().withDisplayName("Repository URL").withDisplayOrder("0");
    }
}
