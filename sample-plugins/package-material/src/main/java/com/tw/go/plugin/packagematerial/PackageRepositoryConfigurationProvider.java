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
package com.tw.go.plugin.packagematerial;


import com.tw.go.plugin.packagematerial.message.PackageMaterialProperties;
import com.tw.go.plugin.packagematerial.message.PackageMaterialProperty;
import com.tw.go.plugin.packagematerial.message.ValidationResultMessage;


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
        //validate configurationProvidedByUser and populate validationResultMessage
        return validationResultMessage;
    }

    public ValidationResultMessage validatePackageConfiguration(PackageMaterialProperties configurationProvidedByUser) {
        ValidationResultMessage validationResultMessage = new ValidationResultMessage();
        //validate configurationProvidedByUser and populate validationResultMessage
        return validationResultMessage;
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

    private PackageMaterialProperty packageSpec() {
        return new PackageMaterialProperty().withDisplayName("Package Spec").withDisplayOrder("0");
    }
}
