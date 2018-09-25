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

package cd.go.artifact.dummy.model;

import cd.go.artifact.dummy.config.Field;
import cd.go.artifact.dummy.config.Metadata;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

import static cd.go.artifact.dummy.DummyArtifactPlugin.GSON;

public class FetchArtifact {
    @SerializedName("Path")
    private String path;

    public String getPath() {
        return path;
    }

    public static String metadata() {
        return GSON.toJson(Arrays.asList(
                new Field("Path",new Metadata(true,false))
        ));
    }

    public static FetchArtifact from(String json) {
        return GSON.fromJson(json, FetchArtifact.class);
    }

    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (StringUtils.isBlank(path)) {
            result.addError("Path", "must be provided.");
        }
        return result;
    }
}
