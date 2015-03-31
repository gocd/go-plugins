package com.tw.go.notification.log;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LogNotificationPluginImplTest {
    @Test
    public void shouldCreateMessageForNotification() {
        LogNotificationPluginImpl plugin = new LogNotificationPluginImpl();
        Map requestBody = getRequestBodyMap("pipeline-name", "1", "stage-name", "1", "Passed", "Passed");
        assertThat(plugin.getMessage(getGoPluginApiRequest(requestBody)), is("[pipeline-name|1|stage-name|1|Passed|Passed]"));
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