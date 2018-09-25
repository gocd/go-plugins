/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.artifact.dummy;

import java.util.Collections;

import static cd.go.artifact.dummy.DummyArtifactPlugin.GSON;
import static cd.go.artifact.dummy.ResourceReader.read;

public class View {
    private String viewPath;

    public View(String viewPath) {
        this.viewPath = viewPath;
    }

    public String toJSON() {
        return GSON.toJson(Collections.singletonMap("template", read(viewPath)));
    }
}
