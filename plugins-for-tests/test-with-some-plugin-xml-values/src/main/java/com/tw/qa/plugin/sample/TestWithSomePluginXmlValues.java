/*************************GO-LICENSE-START*********************************
 * Copyright 2021 ThoughtWorks, Inc.
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

package com.tw.qa.plugin.sample;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.annotation.UnLoad;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.dependency.Console;

import java.util.HashMap;

import static java.util.Arrays.asList;

@Extension
public class TestWithSomePluginXmlValues implements GoPlugin {
    Console console = new Console();
    Logger logger = Logger.getLoggerFor(TestWithSomePluginXmlValues.class);

    static {
        Logger.getLoggerFor(TestWithSomePluginXmlValues.class).info("Boo");
    }

    @Load
    public void onLoad(PluginContext context) {
        logger.info("Boo");
        System.out.println("Plugin with some plugin.xml values loaded");
    }

    @UnLoad
    public void onUnload(PluginContext context) {
        System.out.println("Plugin unloaded");
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {

    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) throws UnhandledRequestTypeException {

        if ("configuration".equals(goPluginApiRequest.requestName())) {
            return new GetTaskPluginConfig().execute();
        } else if ("view".equals(goPluginApiRequest.requestName())) {
            return getViewRequest();
        }
        throw new UnhandledRequestTypeException(goPluginApiRequest.requestName());
    }

    private GoPluginApiResponse getViewRequest(){
        HashMap<String, String> view = new HashMap<>();
        view.put("displayValue", "TestTask");
        view.put("template", "<html><body>with some plugin xml values</body></html>");

        return DefaultGoPluginApiResponse.success(new GsonBuilder().create().toJson(view));
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("task", asList("1.0"));
    }
}
