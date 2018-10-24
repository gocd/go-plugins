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

public class ArtifactStoreConfig {
    @SerializedName("Url")
    private String url;
    @SerializedName("Username")
    private String username;
    @SerializedName("Password")
    private String password;

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (StringUtils.isBlank(url)) {
            result.addError("Url", "must be provided.");
        }

        if (StringUtils.isBlank(username)) {
            result.addError("Username", "must be provided.");
        }

        if (StringUtils.isBlank(password)) {
            result.addError("Password", "must be provided.");
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtifactStoreConfig)) return false;

        ArtifactStoreConfig that = (ArtifactStoreConfig) o;

        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        return password != null ? password.equals(that.password) : that.password == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    public static ArtifactStoreConfig from(String json) {
        return GSON.fromJson(json, ArtifactStoreConfig.class);
    }

    public static String artifactStoreMetadata() {
        return GSON.toJson(Arrays.asList(
                new Field("Url", new Metadata(true, false)),
                new Field("Username", new Metadata(true, false)),
                new Field("Password", new Metadata(true, true))
        ));
    }
}
