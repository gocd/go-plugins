/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tw.go.plugin.material.artifactrepository.yum.exec;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.material.artifactrepository.yum.exec.command.MultiplePackageException;
import com.tw.go.plugin.material.artifactrepository.yum.exec.command.RepoQueryCommand;
import com.tw.go.plugin.material.artifactrepository.yum.exec.command.RepoQueryParams;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.common.util.ListUtil.join;
import static java.util.Arrays.asList;

public class PackageRepositoryPoller {

    private static Logger LOGGER = Logger.getLoggerFor(PackageRepositoryPoller.class);


    private PackageRepositoryConfigurationProvider configurationProvider;

    public PackageRepositoryPoller(PackageRepositoryConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public CheckConnectionResultMessage checkConnectionToRepository(PackageMaterialProperties repositoryConfiguration) {
        ValidationResultMessage validationResultMessage = configurationProvider.validateRepositoryConfiguration(repositoryConfiguration);
        if (validationResultMessage.failure()) {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, validationResultMessage.getMessages());
        }
        RepoUrl url = repoUrl(repositoryConfiguration);
        try {
            url.checkConnection();
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS, asList(String.format("Successfully accessed repository metadata at %s", url.getRepoMetadataUrl())));
        } catch (Exception e) {
            LOGGER.warn(String.format("[Yum Repo Check Connection] Check connection for %s failed with exception - %s", url.getRepoMetadataUrl(), e));
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, asList(String.format("Could not access file - %s. %s", url.getRepoMetadataUrl(), e.getMessage())));
        }
    }

    public CheckConnectionResultMessage checkConnectionToPackage(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        CheckConnectionResultMessage result = checkConnectionToRepository(repositoryConfiguration);
        if (!result.success()) {
            return result;
        }
        try {
            ValidationResultMessage validationResultMessage = configurationProvider.validatePackageConfiguration(packageConfiguration);
            if (validationResultMessage.failure()) {
                return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, validationResultMessage.getMessages());
            }
            PackageRevisionMessage latestRevision = getLatestRevision(packageConfiguration, repositoryConfiguration);
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS, asList(String.format("Found package '%s'.", latestRevision.getRevision())));
        } catch (MultiplePackageException e) {
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, asList(e.getMessage()));
        } catch (Exception e) {
            String message = String.format("Could not find any package that matched '%s'.", packageConfiguration.getProperty(Constants.PACKAGE_SPEC).value());
            LOGGER.warn(message);
            return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, asList(message));
        }
    }

    public PackageRevisionMessage getLatestRevision(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        validateData(packageConfiguration, repositoryConfiguration);
        PackageMaterialProperty packageSpec = packageConfiguration.getProperty(Constants.PACKAGE_SPEC);
        RepoUrl url = repoUrl(repositoryConfiguration);
        url.checkConnection();
        return executeRepoQuery(DigestUtils.md5Hex(url.forDisplay()), url, packageSpec);
    }

    public PackageRevisionMessage getLatestRevisionSince(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration, PackageRevisionMessage previousPackageRevision) {
        PackageRevisionMessage latestRevision = getLatestRevision(packageConfiguration, repositoryConfiguration);
        if (latestRevision.getTimestamp().getTime() > previousPackageRevision.getTimestamp().getTime())
            return latestRevision;
        return null;
    }


    private void validateData(PackageMaterialProperties packageConfiguration, PackageMaterialProperties repositoryConfiguration) {
        ValidationResultMessage repoValidationResultMessage = configurationProvider.validateRepositoryConfiguration(repositoryConfiguration);
        ValidationResultMessage pkgValidationResultMessage = configurationProvider.validatePackageConfiguration(packageConfiguration);
        if (repoValidationResultMessage.failure() || pkgValidationResultMessage.failure()) {
            List<String> errors = new ArrayList<String>();
            errors.addAll(repoValidationResultMessage.getMessages());
            errors.addAll(pkgValidationResultMessage.getMessages());
            String message = join(errors, "; ");
            LOGGER.warn(String.format("Data validation failed: %s", message));
            throw new RuntimeException(message);
        }
    }

    private PackageRevisionMessage executeRepoQuery(String repoId, RepoUrl url, PackageMaterialProperty packageSpec) {
        return new RepoQueryCommand(new RepoQueryParams(repoId, url, packageSpec.value())).execute();
    }

    private RepoUrl repoUrl(PackageMaterialProperties packageMaterialProperties) {
        PackageMaterialProperty repoUrl = packageMaterialProperties.getProperty(Constants.REPO_URL);
        PackageMaterialProperty username = packageMaterialProperties.getProperty(Constants.USERNAME);
        PackageMaterialProperty password = packageMaterialProperties.getProperty(Constants.PASSWORD);
        String usernameValue = username == null ? null : username.value();
        String passwordValue = password == null ? null : password.value();
        return new RepoUrl(repoUrl.value(), usernameValue, passwordValue);
    }
}
