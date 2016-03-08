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

import java.util.List;

import static com.tw.go.plugin.common.util.ListUtil.join;

public class ProcessOutput {
    private int returnCode;
    private List<String> stdOut;
    private List<String> stdErr;

    public ProcessOutput(int returnCode, List<String> stdOut, List<String> stdErr) {
        this.returnCode = returnCode;
        this.stdOut = stdOut;
        this.stdErr = stdErr;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public List<String> getStdOut() {
        return stdOut;
    }

    public List<String> getStdErr() {
        return stdErr;
    }

    public String getStdErrorAsString() {
        if (hasErrors())
            return "Error Message: " + join(getStdErr(), "\n");
        return "";
    }

    public boolean isZeroReturnCode() {
        return returnCode == 0;
    }

    public boolean hasOutput() {
        return stdOut != null && !stdOut.isEmpty();
    }

    public boolean hasErrors() {
        return stdErr != null && !stdErr.isEmpty();
    }

    @Override
    public String toString() {
        return "ProcessOutput{" +
                "returnCode=" + returnCode +
                ", stdOut=" + stdOut +
                ", stdErr=" + stdErr +
                '}';
    }
}
