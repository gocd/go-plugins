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

package com.tw.go.task.curl;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import com.thoughtworks.go.plugin.api.task.TaskView;
import com.thoughtworks.go.plugin.api.config.Configuration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;

@Extension
public class CurlTask implements Task {

    public static final String MARKDOWN_INDEX_PAGE = "http://daringfireball.net/projects/markdown/index.text";
    public static final String URL_PROPERTY = "Url";

    @Override
    public TaskConfig config() {
        TaskConfig config = new TaskConfig();
        config.addProperty(URL_PROPERTY).withDefault(MARKDOWN_INDEX_PAGE);
        return config;
    }

    @Override
    public TaskExecutor executor() {
        return new CurlTaskExecutor();
    }

    @Override
    public TaskView view() {
        TaskView taskView = new TaskView() {
            @Override
            public String displayValue() {
                return "Curl";
            }

            @Override
            public String template() {
                return "<label>URL:</label>\n" +
                        "<input type=\"text\" ng-model=\"Url\"></input>";
            }
        };
        return taskView;
    }

    @Override
    public ValidationResult validate(TaskConfig taskConfig) {
        return new ValidationResult();
    }

}
