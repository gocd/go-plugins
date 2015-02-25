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
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.plugin.task.rake.model.Constants;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;

public class RakeTaskConfigurationHandlerTest {
    @Test
    public void shouldHandleGetConfigRequestCorrectly() {
        GoPluginApiResponse goPluginApiResponse = new RakeTaskConfigurationHandler().handleGetConfigRequest();

        assertThat(goPluginApiResponse.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(goPluginApiResponse.responseBody(), is("{\"build_file\":{\"required\":false,\"display-name\":\"Build File\",\"display-order\":\"0\"},\"target\":{\"required\":false,\"display-name\":\"Target\",\"display-order\":\"1\"},\"working_directory\":{\"required\":false,\"display-name\":\"Working Directory\",\"display-order\":\"2\"}}"));
    }

    @Test
    public void shouldHandleValidationRequestForSuccessCorrectly() {
        Map configMap = new HashMap();
        GoPluginApiResponse goPluginApiResponse = new RakeTaskConfigurationHandler().handleValidation(TestUtil.createGoPluginAPIRequest(configMap));

        assertThat(goPluginApiResponse.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(goPluginApiResponse.responseBody(), is("{\"errors\":{}}"));
    }

    @Test
    public void shouldHandleValidationRequestForErrorCorrectly() {
        Map configMap = new HashMap();
        configMap.put(Constants.CONFIG_WORKING_DIRECTORY, TestUtil.createMapWithValue("/tmp"));
        GoPluginApiResponse goPluginApiResponse = new RakeTaskConfigurationHandler().handleValidation(TestUtil.createGoPluginAPIRequest(configMap));

        assertThat(goPluginApiResponse.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(goPluginApiResponse.responseBody(), is("{\"errors\":{\"working_directory\":\"Working Directory should be a valid file path within the Go Agent working directory.\"}}"));
    }

    @Test
    public void shouldHandleTaskViewRequestCorrectly() {
        RakeTaskConfigurationHandler rakeTaskConfigurationHandler = new RakeTaskConfigurationHandler();
        GoPluginApiResponse goPluginApiResponse = rakeTaskConfigurationHandler.handleTaskView();

        assertThat(goPluginApiResponse.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        Map responseBodyMap = (Map) new GsonBuilder().serializeNulls().create().fromJson(goPluginApiResponse.responseBody(), Object.class);
        assertThat((String) responseBodyMap.get("displayValue"), is(Constants.PLUGIN_DISPLAY_NAME));
        assertThat((String) responseBodyMap.get("template"), is(getViewTemplateContent(rakeTaskConfigurationHandler)));
    }

    @Test
    public void shouldHandleErrorForTaskViewRequestCorrectly() throws IOException {
        RakeTaskConfigurationHandler rakeTaskConfigurationHandler = new RakeTaskConfigurationHandler();
        RakeTaskConfigurationHandler spyRakeTaskConfigurationHandler = spy(rakeTaskConfigurationHandler);
        stub(spyRakeTaskConfigurationHandler.getViewTemplateContent()).toThrow(new RuntimeException("message"));

        GoPluginApiResponse goPluginApiResponse = spyRakeTaskConfigurationHandler.handleTaskView();

        assertThat(goPluginApiResponse.responseCode(), is(DefaultGoPluginApiResponse.INTERNAL_ERROR));
        assertThat(goPluginApiResponse.responseBody(), is("{\"exception\":\"Failed to find template: message\"}"));
    }

    private String getViewTemplateContent(RakeTaskConfigurationHandler rakeTaskConfigurationHandler) {
        String viewTemplateContent = null;
        try {
            viewTemplateContent = rakeTaskConfigurationHandler.getViewTemplateContent();
        } catch (Exception e) {
            fail("failed to read view template content");
        }
        return viewTemplateContent;
    }
}