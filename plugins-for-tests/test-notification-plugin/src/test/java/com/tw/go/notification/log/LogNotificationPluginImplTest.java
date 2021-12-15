package com.tw.go.notification.log;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
public class LogNotificationPluginImplTest {
    @Mock
    private GoApplicationAccessor goApplicationAccessor;

    private LogNotificationPluginImpl plugin;

    @BeforeEach
    public void setUp() {
        plugin = new LogNotificationPluginImpl();
        when(goApplicationAccessor.submit(any(GoApiRequest.class))).thenReturn(getGoApiResponse(new GsonBuilder().create().toJson(getPluginSettingsMap())));
    }

    @Test
    public void shouldCreateMessageForNotification() {
        plugin.initializeGoApplicationAccessor(goApplicationAccessor);

        Map requestBody = getRequestBodyMap("pipeline-name", "1", "stage-name", "1", "Passed", "Passed");
        assertThat(plugin.getMessage(getGoPluginApiRequest(requestBody))).isEqualTo("[pipeline-name|1|stage-name|1|Passed|Passed|value]");
    }

    private Map<String, String> getPluginSettingsMap() {
        Map<String, String> pluginSettings = new HashMap<>();
        pluginSettings.put("append_value", "value");
        return pluginSettings;
    }

    private Map getRequestBodyMap(String pipelineName, String pipelineCounter, String stageName, String stageCounter, String stageState, String stageResult) {
        Map requestBody = new HashMap();
        Map pipelineMap = new HashMap();
        pipelineMap.put("name", pipelineName);
        pipelineMap.put("counter", pipelineCounter);
        Map stageMap = new HashMap();
        stageMap.put("name", stageName);
        stageMap.put("counter", stageCounter);
        stageMap.put("state", stageState);
        stageMap.put("result", stageResult);
        pipelineMap.put("stage", stageMap);
        requestBody.put("pipeline", pipelineMap);
        return requestBody;
    }

    private GoApiResponse getGoApiResponse(final String responseBody) {
        return new GoApiResponse() {
            @Override
            public int responseCode() {
                return 0;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return responseBody;
            }
        };
    }

    private GoPluginApiRequest getGoPluginApiRequest(final Map requestBody) {
        return new GoPluginApiRequest() {
            @Override
            public String extension() {
                return null;
            }

            @Override
            public String extensionVersion() {
                return null;
            }

            @Override
            public String requestName() {
                return null;
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
                return new GsonBuilder().create().toJson(requestBody);
            }
        };
    }
}
