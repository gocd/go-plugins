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
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class TestUtil {
    public static GoPluginApiRequest createGoPluginAPIRequest(final Map requestBody) {
        return new GoPluginApiRequest() {
            @Override
            public String extension() {
                return null;
            }

            @Override
            public String extensionVersion() {
                return null;
            }

            @Override
            public String requestName() {
                return null;
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return new GsonBuilder().serializeNulls().create().toJson(requestBody);
            }
        };
    }

    public static JobConsoleLogger createJobConsoleLogger(final StringWriter outputStreamContents, final StringWriter errorStreamContents) {
        return new JobConsoleLogger() {
            public void printLine(String line) {
            }

            public void printEnvironment(Map<String, String> environment) {
            }

            public void readOutputOf(InputStream in) {
                try {
                    IOUtils.copy(in, outputStreamContents, "UTF-8");
                } catch (Throwable t) {
                    fail("error occurred while reading output stream");
                }
            }

            public void readErrorOf(InputStream in) {
                try {
                    IOUtils.copy(in, errorStreamContents, "UTF-8");
                } catch (Throwable t) {
                    fail("error occurred while reading output stream");
                }
            }
        };
    }

    public static Map createMapWithValue(String value) {
        Map map = new HashMap();
        map.put("value", value);
        return map;
    }
}
