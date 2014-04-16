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

package com.tw.go.plugin.material.artifactrepository.yum.exec.command;

import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.StringContains.containsString;

public class ProcessRunnerTest {
    @Test
    public void shouldRunACommand() {
        ProcessOutput output = new ProcessRunner().execute(new String[]{"echo", "foo"}, new HashMap<String, String>());
        assertThat(output.getStdOut().get(0), is("foo"));
        assertThat(output.getReturnCode(), is(0));
    }

    @Test
    public void shouldThrowExceptionIfCommandThrowsAnException() {
        try {
            new ProcessRunner().execute(new String[]{"doesNotExist"}, new HashMap<String, String>());
            fail("Should have thrown exception");
        } catch (Exception e) {
            assertThat(e instanceof RuntimeException, is(true));
            if (isWindows()) {
                assertThat(e.getMessage(), containsString("'doesNotExist' is not recognized as an internal or external command"));
            } else {
                assertThat(e.getMessage(), containsString("Cannot run program \"doesNotExist\""));
            }
        }
    }

    @Test
    public void shouldReturnErrorOutputIfCommandFails() {
        ProcessOutput output = null;
        if (isWindows()) {
            output = new ProcessRunner().execute(new String[]{"dir", "foo:"}, null);
            assertThat(output.getStdErrorAsString(), containsString("File Not Found"));
        } else {
            output = new ProcessRunner().execute(new String[]{"ls", "/foo"}, new HashMap<String, String>());
            assertThat(output.getStdErrorAsString(), containsString("Error Message: ls: cannot access /foo: No such file or directory"));
        }
        assertThat(output.getReturnCode(), is(not(0)));
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.contains("Windows");
    }
}
