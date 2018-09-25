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

package cd.go.artifact.dummy.request;

import cd.go.artifact.dummy.model.ArtifactStoreConfig;
import cd.go.artifact.dummy.model.FetchArtifact;
import cd.go.artifact.dummy.model.PublishMetadata;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static cd.go.artifact.dummy.DummyArtifactPlugin.GSON;

public class FetchArtifactRequest {
    @Expose
    @SerializedName("store_configuration")
    private ArtifactStoreConfig artifactStoreConfig;
    @Expose
    @SerializedName("fetch_artifact_configuration")
    private FetchArtifact fetchArtifact;
    @Expose
    @SerializedName("artifact_metadata")
    private PublishMetadata metadata;

    public ArtifactStoreConfig getArtifactStoreConfig() {
        return artifactStoreConfig;
    }

    public PublishMetadata getMetadata() {
        return metadata;
    }

    public static FetchArtifactRequest fromJSON(String json) {
        return GSON.fromJson(json, FetchArtifactRequest.class);
    }

    public FetchArtifact getFetchArtifact() {
        return fetchArtifact;
    }
}