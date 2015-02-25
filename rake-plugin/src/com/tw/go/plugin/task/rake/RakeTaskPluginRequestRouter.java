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

import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.plugin.task.rake.model.Constants;

@Extension
public class RakeTaskPluginRequestRouter implements GoPlugin {
    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        if (Constants.REQUEST_CONFIGURATION.equals(request.requestName())) {
            return new RakeTaskConfigurationHandler().handleGetConfigRequest();
        } else if (Constants.REQUEST_VALIDATE.equals(request.requestName())) {
            return new RakeTaskConfigurationHandler().handleValidation(request);
        } else if (Constants.REQUEST_VIEW.equals(request.requestName())) {
            return new RakeTaskConfigurationHandler().handleTaskView();
        } else if (Constants.REQUEST_EXECUTE.equals(request.requestName())) {
            return new RakeTaskExecutor().handleTaskExecution(request);
        }
        throw new UnhandledRequestTypeException(request.requestName());
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(Constants.EXTENSION_NAME, Constants.SUPPORTED_EXTENSION_VERSIONS);
    }
}