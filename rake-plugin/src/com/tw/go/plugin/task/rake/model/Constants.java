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

import java.util.Arrays;
import java.util.List;

public interface Constants {
    public static final String EXTENSION_NAME = "task";
    public static final List<String> SUPPORTED_EXTENSION_VERSIONS = Arrays.asList("1.0");

    public static final String REQUEST_CONFIGURATION = "configuration";
    public static final String REQUEST_VALIDATE = "validate";
    public static final String REQUEST_VIEW = "view";
    public static final String REQUEST_EXECUTE = "execute";

    public static final String CONFIG_BUILD_FILE = "build_file";
    public static final String CONFIG_TARGET = "target";
    public static final String CONFIG_WORKING_DIRECTORY = "working_directory";

    public static final String PLUGIN_DISPLAY_NAME = "Rake";
    public static final String VIEW_TEMPLATE_PATH = "/views/task.template.html";
}
