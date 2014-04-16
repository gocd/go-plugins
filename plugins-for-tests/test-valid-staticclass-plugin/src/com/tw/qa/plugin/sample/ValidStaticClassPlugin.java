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

package com.tw.qa.plugin.sample;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.annotation.Load;
import com.thoughtworks.go.plugin.api.annotation.UnLoad;
import com.thoughtworks.go.plugin.api.info.PluginContext;
import com.thoughtworks.go.plugin.api.info.PluginDescriptor;
import com.thoughtworks.go.plugin.api.info.PluginDescriptorAware;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.dependency.Console;


public class ValidStaticClassPlugin {
    Console console = new Console();
    Logger logger = Logger.getLoggerFor(ValidStaticClassPlugin.class);

    static {

        Logger.getLoggerFor(ValidStaticClassPlugin.class).info("Sample log");

    }


    @Load
    public void onLoad(PluginContext context) {
        logger.info("ValidTopClass");
        System.out.println("ValidStaticClassPlugin loaded");
    }



    @UnLoad
    public void onUnload(PluginContext context) {
        System.out.println("ValidStaticClassPlugin unloaded");
    }

    @Extension
    public static class StaticInnerClassOne implements PluginDescriptorAware {
        static Logger logger = Logger.getLoggerFor(StaticInnerClassOne.class);

        static {
            logger.info("Inside static inner class");
        }
        @Override
        public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {

        }
    }
}
