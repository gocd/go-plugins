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

import com.tw.go.plugin.packagematerial.message.CheckConnectionResultMessage;
import com.tw.go.plugin.packagematerial.message.PackageMaterialProperties;
import com.tw.go.plugin.packagematerial.message.PackageRevisionMessage;

import static com.tw.go.plugin.packagematerial.message.CheckConnectionResultMessage.STATUS.SUCCESS;
import static java.util.Arrays.asList;

public class PackageRepositoryPoller {

    private PackageRepositoryConfigurationProvider configurationProvider;

    public PackageRepositoryPoller(PackageRepositoryConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public CheckConnectionResultMessage checkConnectionToRepository(PackageMaterialProperties repositoryConfiguration) {
        // check repository connection here
        return new CheckConnectionResultMessage(SUCCESS, asList("success message"));
    }

    public CheckConnectionResultMessage checkConnectionToPackage(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        // check package connection here
        return new CheckConnectionResultMessage(SUCCESS, asList("success message"));
    }

    public PackageRevisionMessage getLatestRevision(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        // get latest modification here
        return new PackageRevisionMessage();
    }

    public PackageRevisionMessage getLatestRevisionSince(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration, PackageRevisionMessage previousPackageRevision) {
        // get latest modification since here
        return new PackageRevisionMessage();

    }
}
