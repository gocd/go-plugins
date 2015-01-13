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

import com.tw.go.plugin.task.rake.TestUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ConfigTest {
    @Test
    public void shouldCreateConfigFromMapCorrectly() {
        Map configMap = new HashMap();
        configMap.put(Constants.CONFIG_BUILD_FILE, TestUtil.createMapWithValue("build-file"));
        configMap.put(Constants.CONFIG_TARGET, TestUtil.createMapWithValue("target"));
        configMap.put(Constants.CONFIG_WORKING_DIRECTORY, TestUtil.createMapWithValue("working-directory"));
        Config config = new Config(configMap);

        assertThat(config.getBuildFile(), is("build-file"));
        assertThat(config.getTarget(), is("target"));
        assertThat(config.getWorkingDirectory(), is("working-directory"));
    }
}