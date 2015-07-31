/*************************GO-LICENSE-START*********************************
 * Copyright 2015 ThoughtWorks, Inc.
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

package com.tw.go.plugin.material.artifactrepository.yum.exec;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class YumEnvironmentMap {

    final String defaultTempYumRepoDir = "/var/tmp";
    final String HOME = "HOME";
    final String TMPDIR = "TMPDIR";
    private String packageRepoId;

    public YumEnvironmentMap(String packageRepoId) {
        this.packageRepoId = packageRepoId;
    }

    public Map<String, String> buildYumEnvironmentMap() {
        Map<String, String> envMap = new HashMap<String, String>();

        envMap.put(HOME, getHomeEnvVarValue());
        envMap.put(TMPDIR, getTempRepoFilePath());
        return envMap;
    }

    String getSystemEnvVariableFor(String name) {
        return System.getenv(name);
    }

    String getSystemPropertyValueFor(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    private String getHomeEnvVarValue() {
        String homeEnv = getSystemEnvVariableFor(HOME);
        if (homeEnv == null) {
            homeEnv = getSystemPropertyValueFor("java.io.tmpdir", null);
        }
        return homeEnv;
    }

    private String getTempRepoFilePath() {
        File temporaryRepoFileLocation = new File(getSystemPropertyValueFor("go.yum.tmpdir", defaultTempYumRepoDir), String.format("go-yum-plugin-%s", DigestUtils.md5Hex(packageRepoId)));
        temporaryRepoFileLocation.mkdirs();
        return temporaryRepoFileLocation.getAbsolutePath();
    }
}
