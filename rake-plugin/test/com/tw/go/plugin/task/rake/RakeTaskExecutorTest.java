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

import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import com.tw.go.plugin.task.rake.model.Config;
import com.tw.go.plugin.task.rake.model.Constants;
import com.tw.go.plugin.task.rake.model.Context;
import com.tw.go.plugin.task.rake.model.Result;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class RakeTaskExecutorTest {
    @Test
    public void shouldCreateCommandCorrectly() {
        RakeTaskExecutor rakeTaskExecutor = new RakeTaskExecutor();
        assertThat(rakeTaskExecutor.createCommand(new Config(null, null, null)), is(new String[]{"rake"}));
        assertThat(rakeTaskExecutor.createCommand(new Config("build_file", null, null)), is(new String[]{"rake", "-f", "build_file"}));
        assertThat(rakeTaskExecutor.createCommand(new Config(null, "target", null)), is(new String[]{"rake", "target"}));
        assertThat(rakeTaskExecutor.createCommand(new Config("build_file", "target", null)), is(new String[]{"rake", "-f", "build_file", "target"}));
    }

    @Test
    public void shouldGetWorkingDirectoryCorrectly() {
        RakeTaskExecutor rakeTaskExecutor = new RakeTaskExecutor();
        assertThat(rakeTaskExecutor.getWorkingDirectory(new Context(null, "/tmp"), new Config(null, null, null)).getAbsolutePath(), is("/tmp"));
        assertThat(rakeTaskExecutor.getWorkingDirectory(new Context(null, "/tmp"), new Config(null, null, "dir")).getAbsolutePath(), is("/tmp/dir"));
    }

    @Test
    public void shouldExecuteRakeScriptCorrectly() throws IOException {
        runRakeScriptAndVerify("print_environment_variables", "ENV_VAR_1: 1\nENV_VAR_2: 2\n", null, DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, "Rake execution passed");
    }

    @Test
    public void shouldHandleErrorForExecuteRakeScriptCorrectly() throws IOException {
        runRakeScriptAndVerify("non_existing_target", null, null, DefaultGoPluginApiResponse.INTERNAL_ERROR, "Rake execution failed. Please check the output");
    }

    @Test
    public void shouldHandleTaskExecutionCorrectly() {
        RakeTaskExecutor rakeTaskExecutor = new RakeTaskExecutor();
        RakeTaskExecutor spyRakeTaskExecutor = spy(rakeTaskExecutor);

        doReturn(TestUtil.createJobConsoleLogger(null, null)).when(spyRakeTaskExecutor).getConsoleLogger();
        doReturn(new Result(true, "message")).when(spyRakeTaskExecutor).execute(eq(new Config("build-file", "target", "working-directory")), eq(new Context(null, "agent-directory")), any(JobConsoleLogger.class));

        Map requestBody = new HashMap();
        Map configMap = new HashMap();
        configMap.put(Constants.CONFIG_BUILD_FILE, TestUtil.createMapWithValue("build-file"));
        configMap.put(Constants.CONFIG_TARGET, TestUtil.createMapWithValue("target"));
        configMap.put(Constants.CONFIG_WORKING_DIRECTORY, TestUtil.createMapWithValue("working-directory"));
        requestBody.put("config", configMap);
        Map contextMap = new HashMap();
        contextMap.put("environmentVariables", null);
        contextMap.put("workingDirectory", "agent-directory");
        requestBody.put("context", contextMap);
        GoPluginApiResponse goPluginApiResponse = spyRakeTaskExecutor.handleTaskExecution(TestUtil.createGoPluginAPIRequest(requestBody));

        assertThat(goPluginApiResponse.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(goPluginApiResponse.responseBody(), is("{\"success\":true,\"message\":\"message\",\"exception\":null}"));
    }

    private void runRakeScriptAndVerify(String targetName, String output, String error, int responseCode, String message) throws IOException {
        String systemTmpDir = System.getProperty("java.io.tmpdir");
        String tmpDirForTestName = UUID.randomUUID().toString();
        String sampleRakeScriptName = UUID.randomUUID().toString();
        File tmpDirForTest = new File(systemTmpDir, tmpDirForTestName);
        try {
            setupRakeScript(tmpDirForTest, sampleRakeScriptName);

            final StringWriter outputStreamContents = new StringWriter();
            final StringWriter errorStreamContents = new StringWriter();
            JobConsoleLogger jobConsoleLogger = TestUtil.createJobConsoleLogger(outputStreamContents, errorStreamContents);

            HashMap environmentVariables = new HashMap();
            environmentVariables.put("ENV_VAR_1", "1");
            environmentVariables.put("ENV_VAR_2", "2");
            Result result = new RakeTaskExecutor().execute(new Config(sampleRakeScriptName, targetName, tmpDirForTestName), new Context(environmentVariables, systemTmpDir), jobConsoleLogger);

            if (output != null)
                assertThat(outputStreamContents.toString(), is(output));
            if (error != null)
                assertThat(errorStreamContents.toString(), is(error));
            assertThat(result.responseCode(), is(responseCode));
            assertThat(result.getMessage(), is(message));
        } finally {
            deleteRakeScript(tmpDirForTest);
        }
    }

    private void setupRakeScript(File tmpDirForTest, String sampleRakeScriptName) throws IOException {
        FileUtils.forceMkdir(tmpDirForTest);
        String sampleRakeScript = IOUtils.toString(getClass().getResourceAsStream("/sample-rake-script"), "UTF-8");
        FileUtils.writeStringToFile(new File(tmpDirForTest, sampleRakeScriptName), sampleRakeScript);
    }

    private void deleteRakeScript(File tmpDirForTest) {
        FileUtils.deleteQuietly(tmpDirForTest);
    }
}