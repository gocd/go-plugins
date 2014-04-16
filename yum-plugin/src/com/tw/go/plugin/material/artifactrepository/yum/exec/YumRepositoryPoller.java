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

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.*;
import com.thoughtworks.go.plugin.api.response.Result;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.yum.exec.command.MultiplePackageException;
import com.tw.go.plugin.material.artifactrepository.yum.exec.command.RepoQueryCommand;
import com.tw.go.plugin.material.artifactrepository.yum.exec.command.RepoQueryParams;
import com.tw.go.plugin.material.artifactrepository.yum.exec.config.YumRepositoryConfiguration;
import org.apache.commons.codec.digest.DigestUtils;

public class YumRepositoryPoller implements PackageMaterialPoller {

    private static Logger LOGGER = Logger.getLoggerFor(YumRepositoryPoller.class);

    public PackageRevision getLatestRevision(PackageConfiguration packagePluginConfiguration, RepositoryConfiguration repositoryPluginConfiguration) {
        validateData(repositoryPluginConfiguration, packagePluginConfiguration);
        Property packageSpec = packagePluginConfiguration.get(Constants.PACKAGE_SPEC);
        RepoUrl url = repoUrl(repositoryPluginConfiguration);
        url.checkConnection();
        return executeRepoQuery(DigestUtils.md5Hex(url.forDisplay()), url, packageSpec);
    }

    public PackageRevision latestModificationSince(PackageConfiguration packagePluginConfiguration, RepositoryConfiguration repositoryPluginConfiguration, PackageRevision previouslyKnownRevision) {
        PackageRevision latestRevision = getLatestRevision(packagePluginConfiguration, repositoryPluginConfiguration);

        if (latestRevision.getTimestamp().getTime() > previouslyKnownRevision.getTimestamp().getTime())
            return latestRevision;
        return null;
    }

    public Result checkConnectionToRepository(RepositoryConfiguration repositoryPackageConfiguration) {
        Result repositoryValidationResult = repositoryValidation(repositoryPackageConfiguration);
        if(!repositoryValidationResult.isSuccessful()) {
            return repositoryValidationResult;
        }
        RepoUrl url = repoUrl(repositoryPackageConfiguration);
        try {
            url.checkConnection();
            return new Result().withSuccessMessages(String.format("Successfully accessed repository metadata at %s", url.getRepoMetadataUrl()));
        } catch (Exception e) {
            LOGGER.warn(String.format("[Yum Repo Check Connection] Check connection for %s failed with exception - %s", url.getRepoMetadataUrl(), e));
            return new Result().withErrorMessages(String.format("Could not access file - %s. %s", url.getRepoMetadataUrl(), e.getMessage()));
        }
    }

    public Result checkConnectionToPackage(PackageConfiguration packageConfigurations, RepositoryConfiguration repositoryPackageConfigurations) {
        Result checkConnectionResult = checkConnectionToRepository(repositoryPackageConfigurations);
        if (!checkConnectionResult.isSuccessful()) {
            return checkConnectionResult;
        }
        try {
            Result packageConfigurationValidationResult = packageValidation(packageConfigurations, repositoryPackageConfigurations);
            if (!packageConfigurationValidationResult.isSuccessful()) {
                return packageConfigurationValidationResult;
            }
            PackageRevision latestRevision = getLatestRevision(packageConfigurations, repositoryPackageConfigurations);
            return new Result() .withSuccessMessages(String.format("Found package '%s'.", latestRevision.getRevision()));
        } catch (MultiplePackageException e) {
            return new Result().withErrorMessages(e.getMessage());
        } catch (Exception e) {
            String message = String.format("Could not find any package that matched '%s'.", packageConfigurations.get(Constants.PACKAGE_SPEC).getValue());
            LOGGER.warn(message);
            return new Result().withErrorMessages(message);
        }
    }

    private Result repositoryValidation(RepositoryConfiguration repositoryPackageConfigurations) {
        ValidationResult validationResult = new YumRepositoryConfiguration().isRepositoryConfigurationValid(repositoryPackageConfigurations);
        if (!validationResult.isSuccessful()) {
            return new Result().withErrorMessages(validationResult.getMessages());
        }
        return new Result();
    }

    private Result packageValidation(PackageConfiguration packageConfigurations, RepositoryConfiguration repositoryPackageConfiguration) {
        ValidationResult validationResult = new YumRepositoryConfiguration().isPackageConfigurationValid(packageConfigurations, repositoryPackageConfiguration);
        if (!validationResult.isSuccessful()) {
            return new Result().withErrorMessages(validationResult.getMessages());
        }
        return new Result();
    }

    private void validateData(RepositoryConfiguration repositoryConfigurations, PackageConfiguration packageConfigurations) {
        ValidationResult validationResult = new ValidationResult();
        new YumRepositoryConfiguration().validate(packageConfigurations, repositoryConfigurations, validationResult);
        if (!validationResult.isSuccessful()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : validationResult.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            String message = errorString.substring(0, errorString.length() - 2);
            LOGGER.warn(String.format("Data validation failed: %s", message));
            throw new RuntimeException(message);
        }
    }

    PackageRevision executeRepoQuery(String repoId, RepoUrl url, Property packageSpec) {
        return new RepoQueryCommand(new RepoQueryParams(repoId, url, packageSpec.getValue())).execute();
    }

    private RepoUrl repoUrl(RepositoryConfiguration repositoryPluginConfigurations) {
        Property repoUrl = repositoryPluginConfigurations.get(Constants.REPO_URL);
        Property username = repositoryPluginConfigurations.get(Constants.USERNAME);
        Property password = repositoryPluginConfigurations.get(Constants.PASSWORD);
        String usernameValue = username == null ? null : username.getValue();
        String passwordValue = password == null ? null : password.getValue();
        return new RepoUrl(repoUrl.getValue(), usernameValue, passwordValue);
    }
}
