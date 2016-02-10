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

package com.tw.go.plugin.material.artifactrepository.yum.exec.command;

import org.apache.commons.io.IOUtils;

import java.util.List;
import java.util.Map;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class ProcessRunner {
    public ProcessOutput execute(String[] command, Map<String, String> envMap) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = null;
        ProcessOutput processOutput = null;
        try {
            processBuilder.environment().putAll(envMap);
            process = processBuilder.start();
            int returnCode = process.waitFor();
            List outputStream = IOUtils.readLines(process.getInputStream());
            List errorStream = IOUtils.readLines(process.getErrorStream());
            processOutput = new ProcessOutput(returnCode, outputStream, errorStream);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (process != null) {
                closeQuietly(process.getInputStream());
                closeQuietly(process.getErrorStream());
                closeQuietly(process.getOutputStream());
                process.destroy();
            }
        }
        return processOutput;
    }
}
