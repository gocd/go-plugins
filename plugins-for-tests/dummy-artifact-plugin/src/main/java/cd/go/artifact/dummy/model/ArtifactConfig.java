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

public class ArtifactConfig {
    @SerializedName("Source")
    private String source;

    @SerializedName("Destination")
    private String destination;

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtifactConfig)) return false;

        ArtifactConfig that = (ArtifactConfig) o;

        return source != null ? source.equals(that.source) : that.source == null;
    }

    @Override
    public int hashCode() {
        return source != null ? source.hashCode() : 0;
    }

    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (StringUtils.isBlank(source)) {
            result.addError("Source", "must be provided.");
        }

        if (StringUtils.isBlank(destination)) {
            result.addError("Destination", "must be provided.");
        }

        return result;
    }

    public static ArtifactConfig from(String json) {
        return GSON.fromJson(json, ArtifactConfig.class);
    }

    public static String artifactConfigMetadata() {
        return GSON.toJson(Arrays.asList(
                new Field("Source", new Metadata(true, false)),
                new Field("Destination", new Metadata(true, false))
        ));
    }
}
