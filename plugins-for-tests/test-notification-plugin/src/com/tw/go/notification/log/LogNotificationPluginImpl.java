package com.tw.go.notification.log;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;

@Extension
public class LogNotificationPluginImpl implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(LogNotificationPluginImpl.class);

    public static final String PLUGIN_ID = "log.notifier";
    public static final String EXTENSION_NAME = "notification";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final String PLUGIN_SETTINGS_GET_CONFIGURATION = "go.plugin-settings.get-configuration";
    public static final String PLUGIN_SETTINGS_GET_VIEW = "go.plugin-settings.get-view";
    public static final String PLUGIN_SETTINGS_VALIDATE_CONFIGURATION = "go.plugin-settings.validate-configuration";
    public static final String GET_PLUGIN_SETTINGS = "go.processor.plugin-settings.get";
    public static final String REQUEST_NOTIFICATIONS_INTERESTED_IN = "notifications-interested-in";
    public static final String REQUEST_STAGE_STATUS = "stage-status";

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;
    private GoApplicationAccessor goApplicationAccessor;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        if (goPluginApiRequest.requestName().equals(PLUGIN_SETTINGS_GET_CONFIGURATION)) {
            return handleGetPluginSettingsConfiguration();
        } else if (goPluginApiRequest.requestName().equals(PLUGIN_SETTINGS_GET_VIEW)) {
            try {
                return handleGetPluginSettingsView();
            } catch (IOException e) {
                String message = "Failed to find template: " + e.getMessage();
                return renderJSON(500, message);
            }
        } else if (goPluginApiRequest.requestName().equals(PLUGIN_SETTINGS_VALIDATE_CONFIGURATION)) {
            return handleValidatePluginSettingsConfiguration(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(REQUEST_NOTIFICATIONS_INTERESTED_IN)) {
            return handleNotificationsInterestedIn();
        } else if (goPluginApiRequest.requestName().equals(REQUEST_STAGE_STATUS)) {
            return handleStageNotification(goPluginApiRequest);
        }
        return null;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return getGoPluginIdentifier();
    }

    private GoPluginApiResponse handleGetPluginSettingsConfiguration() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("append_value", createField("Append Value", null, true, false, "0"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleGetPluginSettingsView() throws IOException {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("template", IOUtils.toString(getClass().getResourceAsStream("/plugin-settings.template.html"), "UTF-8"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleValidatePluginSettingsConfiguration(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> responseMap = getMapFor(goPluginApiRequest);
        final Map<String, String> configuration = keyValuePairs(responseMap, "plugin-settings");
        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                validateValue(configuration, fieldValidation);
            }
        });

        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private void validateValue(Map<String, String> configuration, Map<String, Object> fieldMap) {
        if (configuration.get("append_value") == null || configuration.get("append_value").isEmpty()) {
            fieldMap.put("key", "append_value");
            fieldMap.put("message", "'Append Value' is a required field");
        }
    }

    private GoPluginApiResponse handleNotificationsInterestedIn() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("notifications", Arrays.asList(REQUEST_STAGE_STATUS));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleStageNotification(GoPluginApiRequest goPluginApiRequest) {
        int responseCode = SUCCESS_RESPONSE_CODE;
        Map<String, Object> response = new HashMap<String, Object>();
        List<String> messages = new ArrayList<String>();
        try {
            String message = getMessage(goPluginApiRequest);

            LOGGER.warn(message);

            response.put("status", "success");
            messages.add("Could connect to URL successfully");
        } catch (Exception e) {
            responseCode = INTERNAL_ERROR_RESPONSE_CODE;
            response.put("status", "failure");
            messages.add(e.getMessage());
        }

        response.put("messages", messages);
        return renderJSON(responseCode, response);
    }

    String getMessage(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> dataMap = getMapFor(goPluginApiRequest);
        Map pipelineMap = (Map) dataMap.get("pipeline");
        Map stageMap = (Map) pipelineMap.get("stage");
        String pipelineName = (String) pipelineMap.get("name");
        String pipelineCounter = (String) pipelineMap.get("counter");
        String stageName = (String) stageMap.get("name");
        String stageCounter = (String) stageMap.get("counter");
        String stageState = (String) stageMap.get("state");
        String stageResult = (String) stageMap.get("result");

        GoApiResponse response = goApplicationAccessor.submit(getGoApiRequest(GET_PLUGIN_SETTINGS, requestWithPluginId()));
        Map<String, String> responseMap = new GsonBuilder().create().fromJson(response.responseBody(), Map.class);

        return String.format("[%s|%s|%s|%s|%s|%s|%s]", pipelineName, pipelineCounter, stageName, stageCounter, stageState, stageResult, responseMap.get("append_value"));
    }

    private String requestWithPluginId() {
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("plugin-id", PLUGIN_ID);
        return new GsonBuilder().create().toJson(requestMap);
    }

    private Map<String, Object> getMapFor(GoPluginApiRequest goPluginApiRequest) {
        return (Map<String, Object>) new GsonBuilder().create().fromJson(goPluginApiRequest.requestBody(), Object.class);
    }

    private Map<String, Object> createField(String displayName, String defaultValue, boolean isRequired, boolean isSecure, String displayOrder) {
        Map<String, Object> fieldProperties = new HashMap<String, Object>();
        fieldProperties.put("display-name", displayName);
        fieldProperties.put("default-value", defaultValue);
        fieldProperties.put("required", isRequired);
        fieldProperties.put("secure", isSecure);
        fieldProperties.put("display-order", displayOrder);
        return fieldProperties;
    }

    private Map<String, String> keyValuePairs(Map<String, Object> map, String mainKey) {
        Map<String, String> keyValuePairs = new HashMap<String, String>();
        Map<String, Object> fieldsMap = (Map<String, Object>) map.get(mainKey);
        for (String field : fieldsMap.keySet()) {
            Map<String, Object> fieldProperties = (Map<String, Object>) fieldsMap.get(field);
            String value = (String) fieldProperties.get("value");
            keyValuePairs.put(field, value);
        }
        return keyValuePairs;
    }

    private void validate(List<Map<String, Object>> response, FieldValidator fieldValidator) {
        Map<String, Object> fieldValidation = new HashMap<String, Object>();
        fieldValidator.validate(fieldValidation);
        if (!fieldValidation.isEmpty()) {
            response.add(fieldValidation);
        }
    }

    private GoPluginIdentifier getGoPluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
    }

    private GoApiRequest getGoApiRequest(final String api, final String requestBody) {
        return new GoApiRequest() {
            @Override
            public String api() {
                return api;
            }

            @Override
            public String apiVersion() {
                return "1.0";
            }

            @Override
            public GoPluginIdentifier pluginIdentifier() {
                return getGoPluginIdentifier();
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return requestBody;
            }
        };
    }

    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }
}
