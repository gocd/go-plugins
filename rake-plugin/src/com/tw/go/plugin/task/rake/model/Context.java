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

package com.tw.go.plugin.task.rake.model;

import java.util.Map;

public class Context {
    private final Map environmentVariables;
    private final String workingDirectory;

    public Context(Map context) {
        environmentVariables = (Map) context.get("environmentVariables");
        workingDirectory = (String) context.get("workingDirectory");
    }

    public Context(Map environmentVariables, String workingDirectory) {
        this.environmentVariables = environmentVariables;
        this.workingDirectory = workingDirectory;
    }

    public Map getEnvironmentVariables() {
        return environmentVariables;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Context context = (Context) o;

        if (environmentVariables != null ? !environmentVariables.equals(context.environmentVariables) : context.environmentVariables != null)
            return false;
        if (workingDirectory != null ? !workingDirectory.equals(context.workingDirectory) : context.workingDirectory != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = environmentVariables != null ? environmentVariables.hashCode() : 0;
        result = 31 * result + (workingDirectory != null ? workingDirectory.hashCode() : 0);
        return result;
    }
}