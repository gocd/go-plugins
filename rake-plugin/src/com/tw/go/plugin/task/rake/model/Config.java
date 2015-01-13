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

public class Config {
    private final String buildFile;
    private final String target;
    private final String workingDirectory;

    public Config(Map config) {
        buildFile = getValue(config, Constants.CONFIG_BUILD_FILE);
        target = getValue(config, Constants.CONFIG_TARGET);
        workingDirectory = getValue(config, Constants.CONFIG_WORKING_DIRECTORY);
    }

    public Config(String buildFile, String target, String workingDirectory) {
        this.buildFile = buildFile;
        this.target = target;
        this.workingDirectory = workingDirectory;
    }

    private String getValue(Map config, String property) {
        return (String) ((Map) config.get(property)).get("value");
    }

    public String getBuildFile() {
        return buildFile;
    }

    public String getTarget() {
        return target;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Config config = (Config) o;

        if (buildFile != null ? !buildFile.equals(config.buildFile) : config.buildFile != null) return false;
        if (target != null ? !target.equals(config.target) : config.target != null) return false;
        if (workingDirectory != null ? !workingDirectory.equals(config.workingDirectory) : config.workingDirectory != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = buildFile != null ? buildFile.hashCode() : 0;
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (workingDirectory != null ? workingDirectory.hashCode() : 0);
        return result;
    }
}