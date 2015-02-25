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
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import com.tw.go.plugin.common.util.StringUtil;
import com.tw.go.plugin.task.rake.model.Config;
import com.tw.go.plugin.task.rake.model.Context;
import com.tw.go.plugin.task.rake.model.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RakeTaskExecutor {
    public GoPluginApiResponse handleTaskExecution(GoPluginApiRequest request) {
        Map executionRequestMap = (Map) new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        Map configMap = (Map) executionRequestMap.get("config");
        Map contextMap = (Map) executionRequestMap.get("context");

        Result result = execute(new Config(configMap), new Context(contextMap), getConsoleLogger());

        DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(result.responseCode());
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(result.toMap()));
        return response;
    }

    JobConsoleLogger getConsoleLogger() {
        return JobConsoleLogger.getConsoleLogger();
    }

    Result execute(Config config, Context context, JobConsoleLogger console) {
        Process process = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(createCommand(config));
            processBuilder.directory(getWorkingDirectory(context, config));
            processBuilder.environment().putAll(context.getEnvironmentVariables());

            console.printLine("Executing Command: " + processBuilder.command());

            process = processBuilder.start();
            console.readOutputOf(process.getInputStream());
            console.readErrorOf(process.getErrorStream());

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return new Result(false, "Rake execution failed. Please check the output");
            }

            return new Result(true, "Rake execution passed");
        } catch (Exception e) {
            return new Result(false, "Rake execution failed", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    String[] createCommand(Config taskConfig) {
        List<String> command = new ArrayList<String>();
        command.add("rake");
        if (StringUtil.isNotBlank(taskConfig.getBuildFile())) {
            command.add("-f");
            command.add(taskConfig.getBuildFile());
        }
        if (StringUtil.isNotBlank(taskConfig.getTarget())) {
            command.add(taskConfig.getTarget());
        }
        return command.toArray(new String[command.size()]);
    }

    File getWorkingDirectory(Context taskContext, Config taskConfig) {
        if (StringUtil.isBlank(taskConfig.getWorkingDirectory())) {
            return new File(taskContext.getWorkingDirectory());
        }
        return new File(taskContext.getWorkingDirectory(), taskConfig.getWorkingDirectory());
    }
}