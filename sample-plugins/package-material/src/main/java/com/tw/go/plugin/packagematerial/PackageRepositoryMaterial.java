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

import com.thoughtworks.go.plugin.api.AbstractGoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.plugin.packagematerial.message.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse.success;
import static com.tw.go.plugin.packagematerial.JsonUtil.fromJsonString;
import static com.tw.go.plugin.packagematerial.JsonUtil.toJsonString;
import static java.util.Arrays.asList;

@Extension
public class PackageRepositoryMaterial extends AbstractGoPlugin {

    public static final String EXTENSION = "package-repository";
    public static final String REQUEST_REPOSITORY_CONFIGURATION = "repository-configuration";
    public static final String REQUEST_PACKAGE_CONFIGURATION = "package-configuration";
    public static final String REQUEST_VALIDATE_REPOSITORY_CONFIGURATION = "validate-repository-configuration";
    public static final String REQUEST_VALIDATE_PACKAGE_CONFIGURATION = "validate-package-configuration";
    public static final String REQUEST_CHECK_REPOSITORY_CONNECTION = "check-repository-connection";
    public static final String REQUEST_CHECK_PACKAGE_CONNECTION = "check-package-connection";
    public static final String REQUEST_LATEST_PACKAGE_REVISION = "latest-revision";
    public static final String REQUEST_LATEST_PACKAGE_REVISION_SINCE = "latest-revision-since";

    private Map<String, MessageHandler> handlerMap = new LinkedHashMap<String, MessageHandler>();
    private PackageRepositoryConfigurationProvider configurationProvider;
    private final PackageRepositoryPoller packageRepositoryPoller;

    public PackageRepositoryMaterial() {
        configurationProvider = new PackageRepositoryConfigurationProvider();
        packageRepositoryPoller = new PackageRepositoryPoller(configurationProvider);
        handlerMap.put(REQUEST_REPOSITORY_CONFIGURATION, repositoryConfigurationsMessageHandler());
        handlerMap.put(REQUEST_PACKAGE_CONFIGURATION, packageConfigurationMessageHandler());
        handlerMap.put(REQUEST_VALIDATE_REPOSITORY_CONFIGURATION, validateRepositoryConfigurationMessageHandler());
        handlerMap.put(REQUEST_VALIDATE_PACKAGE_CONFIGURATION, validatePackageConfigurationMessageHandler());
        handlerMap.put(REQUEST_CHECK_REPOSITORY_CONNECTION, checkRepositoryConnectionMessageHandler());
        handlerMap.put(REQUEST_CHECK_PACKAGE_CONNECTION, checkPackageConnectionMessageHandler());
        handlerMap.put(REQUEST_LATEST_PACKAGE_REVISION, latestRevisionMessageHandler());
        handlerMap.put(REQUEST_LATEST_PACKAGE_REVISION_SINCE, latestRevisionSinceMessageHandler());
    }


    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        try {
            if (handlerMap.containsKey(goPluginApiRequest.requestName())) {
                return handlerMap.get(goPluginApiRequest.requestName()).handle(goPluginApiRequest);
            }
            return DefaultGoPluginApiResponse.badRequest(String.format("Invalid request name %s", goPluginApiRequest.requestName()));
        } catch (Throwable e) {
            return DefaultGoPluginApiResponse.error(e.getMessage());
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION, asList("1.0"));
    }

    MessageHandler packageConfigurationMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {
                return success(toJsonString(configurationProvider.packageConfiguration().getPropertyMap()));
            }
        };

    }

    MessageHandler repositoryConfigurationsMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {
                return success(toJsonString(configurationProvider.repositoryConfiguration().getPropertyMap()));
            }
        };
    }

    MessageHandler validateRepositoryConfigurationMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {

                ValidateRepositoryConfigurationMessage message = fromJsonString(request.requestBody(), ValidateRepositoryConfigurationMessage.class);
                ValidationResultMessage validationResultMessage = configurationProvider.validateRepositoryConfiguration(message.getRepositoryConfiguration());
                if (validationResultMessage.failure()) {
                    return success(toJsonString(validationResultMessage.getValidationErrors()));
                }
                return success("");
            }
        };
    }

    MessageHandler validatePackageConfigurationMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {
                ValidatePackageConfigurationMessage message = fromJsonString(request.requestBody(), ValidatePackageConfigurationMessage.class);
                ValidationResultMessage validationResultMessage = configurationProvider.validatePackageConfiguration(message.getPackageConfiguration());
                if (validationResultMessage.failure()) {
                    return success(toJsonString(validationResultMessage.getValidationErrors()));
                }
                return success("");
            }
        };
    }

    MessageHandler checkRepositoryConnectionMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {
                RepositoryConnectionMessage message = fromJsonString(request.requestBody(), RepositoryConnectionMessage.class);
                CheckConnectionResultMessage result = packageRepositoryPoller.checkConnectionToRepository(message.getRepositoryConfiguration());
                return success(toJsonString(result));
            }
        };
    }

    MessageHandler checkPackageConnectionMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {
                PackageConnectionMessage message = fromJsonString(request.requestBody(), PackageConnectionMessage.class);
                CheckConnectionResultMessage result = packageRepositoryPoller.checkConnectionToPackage(message.getPackageConfiguration(), message.getRepositoryConfiguration());
                return success(toJsonString(result));
            }
        };
    }

    MessageHandler latestRevisionMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {
                LatestPackageRevisionMessage message = fromJsonString(request.requestBody(), LatestPackageRevisionMessage.class);
                PackageRevisionMessage revision = packageRepositoryPoller.getLatestRevision(message.getPackageConfiguration(), message.getRepositoryConfiguration());
                return success(toJsonString(revision));
            }
        };
    }

    MessageHandler latestRevisionSinceMessageHandler() {
        return new MessageHandler() {
            @Override
            public GoPluginApiResponse handle(GoPluginApiRequest request) {
                LatestPackageRevisionSinceMessage message = fromJsonString(request.requestBody(), LatestPackageRevisionSinceMessage.class);
                PackageRevisionMessage revision = packageRepositoryPoller.getLatestRevisionSince(message.getPackageConfiguration(), message.getRepositoryConfiguration(), message.getPreviousRevision());
                return success(revision == null ? null : toJsonString(revision));
            }
        };
    }

}
