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

package com.tw.go.plugin.task.rake;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.common.util.FileUtil;
import com.tw.go.plugin.common.util.StringUtil;
import com.tw.go.plugin.task.rake.model.Constants;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class RakeTaskConfigurationHandler {
    private static final Logger LOGGER = Logger.getLoggerFor(RakeTaskConfigurationHandler.class);

    public GoPluginApiResponse handleGetConfigRequest() {
        Map config = new LinkedHashMap();
        createProperty(config, Constants.CONFIG_BUILD_FILE, false, "Build File", "0");
        createProperty(config, Constants.CONFIG_TARGET, false, "Target", "1");
        createProperty(config, Constants.CONFIG_WORKING_DIRECTORY, false, "Working Directory", "2");
        return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, config);
    }

    private void createProperty(Map config, String propertyKey, boolean isRequired, String displayName, String displayOrder) {
        Map map = new LinkedHashMap();
        map.put("required", isRequired);
        map.put("display-name", displayName);
        map.put("display-order", displayOrder);
        config.put(propertyKey, map);
    }

    public GoPluginApiResponse handleValidation(GoPluginApiRequest request) {
        Map configMap = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);

        Map errorMap = new LinkedHashMap();
        validateWorkingDirectory(configMap, errorMap);

        Map validationResult = new LinkedHashMap();
        validationResult.put("errors", errorMap);
        return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, validationResult);
    }

    private void validateWorkingDirectory(Map configMap, Map errorMap) {
        Map configKeyMap = (Map) configMap.get(Constants.CONFIG_WORKING_DIRECTORY);
        if (configKeyMap != null && StringUtil.isNotBlank((String) configKeyMap.get("value"))) {
            String workingDirectory = (String) configKeyMap.get("value");
            boolean isFolderInsideSandbox = false;
            try {
                isFolderInsideSandbox = FileUtil.isFolderInsideSandbox(workingDirectory);
            } catch (Exception e) {
            }
            if (!isFolderInsideSandbox) {
                errorMap.put(Constants.CONFIG_WORKING_DIRECTORY, "Working Directory should be a valid file path within the Go Agent working directory.");
            }
        }
    }

    public GoPluginApiResponse handleTaskView() {
        try {
            Map view = new LinkedHashMap();
            view.put("displayValue", Constants.PLUGIN_DISPLAY_NAME);
            view.put("template", getViewTemplateContent());
            return createResponse(DefaultGoApiResponse.SUCCESS_RESPONSE_CODE, view);
        } catch (Exception e) {
            Map view = new LinkedHashMap();
            String errorMessage = "Failed to find template: ";
            view.put("exception", errorMessage + e.getMessage());
            LOGGER.error(errorMessage, e);
            return createResponse(DefaultGoApiResponse.INTERNAL_ERROR, view);
        }
    }

    String getViewTemplateContent() throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream(Constants.VIEW_TEMPLATE_PATH), "UTF-8");
    }

    private GoPluginApiResponse createResponse(int responseCode, Map body) {
        final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
        return response;
    }
}
