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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ContextTest {
    @Test
    public void shouldCreateContextFromMapCorrectly() {
        Map contextMap = new HashMap();
        Map environmentVariables = new HashMap();
        contextMap.put("environmentVariables", environmentVariables);
        contextMap.put("workingDirectory", "agent-directory");
        Context context = new Context(contextMap);

        assertThat(context.getEnvironmentVariables(), is(environmentVariables));
        assertThat(context.getWorkingDirectory(), is("agent-directory"));
    }
}