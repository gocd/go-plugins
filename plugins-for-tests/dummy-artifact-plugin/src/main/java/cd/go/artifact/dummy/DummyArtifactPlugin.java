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

import cd.go.artifact.dummy.model.*;
import cd.go.artifact.dummy.request.FetchArtifactRequest;
import cd.go.artifact.dummy.request.PublishArtifactRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static cd.go.artifact.dummy.model.ArtifactStoreConfig.artifactStoreMetadata;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@Extension
public class DummyArtifactPlugin implements GoPlugin {
    public static final Gson GSON = new Gson();
    public static final Logger LOG = Logger.getLoggerFor(DummyArtifactPlugin.class);
    public static final OkHttpClient CLIENT = new OkHttpClient();

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        final RequestFromServer requestFromServer = RequestFromServer.from(request.requestName());
        try {
            switch (requestFromServer) {
                case REQUEST_GET_CAPABILITIES:
                    return DefaultGoPluginApiResponse.success("{}");
                case REQUEST_STORE_CONFIG_METADATA:
                    return DefaultGoPluginApiResponse.success(artifactStoreMetadata());
                case REQUEST_STORE_CONFIG_VIEW:
                    return DefaultGoPluginApiResponse.success(new View("/artifact-store.template.html").toJSON());
                case REQUEST_STORE_CONFIG_VALIDATE:
                    return DefaultGoPluginApiResponse.success(ArtifactStoreConfig.from(request.requestBody()).validate().toJSON());
                case REQUEST_PUBLISH_ARTIFACT_METADATA:
                    return DefaultGoPluginApiResponse.success(ArtifactConfig.artifactConfigMetadata());
                case REQUEST_PUBLISH_ARTIFACT_VIEW:
                    return DefaultGoPluginApiResponse.success(new View("/publish-artifact.template.html").toJSON());
                case REQUEST_PUBLISH_ARTIFACT_VALIDATE:
                    return DefaultGoPluginApiResponse.success(ArtifactConfig.from(request.requestBody()).validate().toJSON());
                case REQUEST_FETCH_ARTIFACT_METADATA:
                    return DefaultGoPluginApiResponse.success(FetchArtifact.metadata());
                case REQUEST_FETCH_ARTIFACT_VIEW:
                    return DefaultGoPluginApiResponse.success(new View("/fetch-artifact.template.html").toJSON());
                case REQUEST_FETCH_ARTIFACT_VALIDATE:
                    return DefaultGoPluginApiResponse.success(FetchArtifact.from(request.requestBody()).validate().toJSON());
                case REQUEST_PUBLISH_ARTIFACT:
                    return publishArtifact(PublishArtifactRequest.fromJSON(request.requestBody()));
                case REQUEST_FETCH_ARTIFACT:
                    return fetchArtifact(FetchArtifactRequest.fromJSON(request.requestBody()));
                case REQUEST_GET_PLUGIN_ICON:
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("content_type", "image/jpg");
                    jsonObject.addProperty("data", Base64.getEncoder().encodeToString(ResourceReader.readBytes("/icon.jpg")));
                    return DefaultGoPluginApiResponse.success(jsonObject.toString());
                default:
                    throw new RuntimeException("Error while executing request" + request.requestName());
            }
        } catch (Exception e) {
            LOG.error("Error while executing request " + request.requestName(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("artifact", singletonList("2.0"));
    }

    private GoPluginApiResponse fetchArtifact(FetchArtifactRequest fetchArtifactRequest) throws IOException {
        ArtifactStoreConfig artifactStoreConfig = fetchArtifactRequest.getArtifactStoreConfig();
        String artifactPath = fetchArtifactRequest.getFetchArtifact().getPath();
        PublishMetadata metadata = fetchArtifactRequest.getMetadata();

        HttpUrl httpUrl = HttpUrl.parse(artifactStoreConfig.getUrl())
                .newBuilder()
                .addPathSegment("files")
                .addPathSegment(metadata.getJobIdentifier().getPipeline())
                .addPathSegment(metadata.getJobIdentifier().getPipelineCounter())
                .addPathSegment(metadata.getJobIdentifier().getStage())
                .addPathSegment(metadata.getJobIdentifier().getStageCounter())
                .addPathSegment(metadata.getJobIdentifier().getJob())
                .addPathSegments(artifactPath)
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .get()
                .addHeader("Authorization", Credentials.basic(artifactStoreConfig.getUsername(), artifactStoreConfig.getPassword()))
                .addHeader("Confirm", "true")
                .build();

        Response response = CLIENT.newCall(request).execute();
        if (!response.isRedirect() && response.isSuccessful()) {
            return DefaultGoPluginApiResponse.success("[\n" +
                    "  {\n" +
                    "    \"name\": \"VAR1\",\n" +
                    "    \"value\": \"VALUE1\",\n" +
                    "    \"secure\": true\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"VAR2\",\n" +
                    "    \"value\": \"VALUE2\",\n" +
                    "    \"secure\": false\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"name\": \"GO_JOB_NAME\",\n" +
                    "    \"value\": \"new job name\",\n" +
                    "    \"secure\": false\n" +
                    "  }\n" +
                    "]\n");
        }

        LOG.error(format("Failed to fetch artifact[%s] with status %d. Error: %s", artifactPath, response.code(), response.body().string()));
        return DefaultGoPluginApiResponse.error(response.body().string());
    }

    private GoPluginApiResponse publishArtifact(PublishArtifactRequest publishArtifactRequest) {
        ArtifactConfig artifactConfig = publishArtifactRequest.getArtifactPlan().getArtifactConfig();
        ArtifactStoreConfig artifactStoreConfig = publishArtifactRequest.getArtifactStore().getArtifactStoreConfig();
        JobIdentifier jobIdentifier = publishArtifactRequest.getJobIdentifier();

        try {
            File artifact = new File(publishArtifactRequest.getAgentWorkingDir() + File.separator + artifactConfig.getSource());

            if (!artifact.exists()) {
                return DefaultGoPluginApiResponse.error(format("Artifact `%s` does not exist at location `%s`.", artifact.getName(), artifact.getParentFile().getAbsolutePath()));
            }

            if (!artifact.canRead()) {
                return DefaultGoPluginApiResponse.error(format("Does not have permission to read artifact `%s`", artifact.getName()));
            }

            if (artifact.isDirectory()) {
                return DefaultGoPluginApiResponse.error(format("Artifact `%s` is a directory.", artifact.getName()));
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/java-archive"), artifact);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", artifactConfig.getSource(), body)
                    .build();

            HttpUrl httpUrl = HttpUrl.parse(artifactStoreConfig.getUrl())
                    .newBuilder()
                    .addPathSegment("files")
                    .addPathSegment(jobIdentifier.getPipeline())
                    .addPathSegment(jobIdentifier.getPipelineCounter())
                    .addPathSegment(jobIdentifier.getStage())
                    .addPathSegment(jobIdentifier.getStageCounter())
                    .addPathSegment(jobIdentifier.getJob())
                    .addPathSegment(artifactConfig.getDestination())
                    .addPathSegment(new File(artifactConfig.getSource()).getName())
                    .build();


            Request request = new Request.Builder()
                    .url(httpUrl)
                    .post(requestBody)
                    .addHeader("Authorization", Credentials.basic(artifactStoreConfig.getUsername(), artifactStoreConfig.getPassword()))
                    .addHeader("Confirm", "true")
                    .build();

            Response response = CLIENT.newCall(request).execute();
            if (!response.isRedirect() && response.isSuccessful()) {
                return DefaultGoPluginApiResponse.success(GSON.toJson(singletonMap("metadata", new PublishMetadata(artifactConfig.getSource(), jobIdentifier))));
            }

            LOG.error(format("Failed to upload artifact[%s] with status %d. Error: %s", artifactConfig.getSource(), response.code(), response.body().string()));
            return DefaultGoPluginApiResponse.error(response.body().string());
        } catch (Exception e) {
            LOG.error(format("Failed to upload artifact[%s] Error: %s", artifactConfig.getSource(), e.getMessage()));
            return DefaultGoPluginApiResponse.error(e.getMessage());
        }
    }
}
