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

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;
import java.util.Map;

import static cd.go.artifact.dummy.DummyArtifactPlugin.GSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DummyArtifactPluginTest {

    @Test
    void shouldReturnExtensionTypeAsArtifact() {
        assertThat(new DummyArtifactPlugin().pluginIdentifier().getExtension()).isEqualTo("artifact");
    }

    @Test
    void shouldReturn1dot0asSupportedVersion() {
        assertThat(new DummyArtifactPlugin().pluginIdentifier().getSupportedExtensionVersions())
                .hasSize(1)
                .contains("1.0");
    }

    @Test
    void shouldReturnCapabilitiesOfThePlugin() throws UnhandledRequestTypeException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_GET_CAPABILITIES.getRequestName());

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo("{}");
    }

    @Test
    void shouldReturnArtifactStoreMetadata() throws UnhandledRequestTypeException {
        final String expectedMetadata = "[{\"key\":\"Url\",\"metadata\":{\"required\":true,\"secure\":false}},{\"key\":\"Username\",\"metadata\":{\"required\":true,\"secure\":false}},{\"key\":\"Password\",\"metadata\":{\"required\":true,\"secure\":true}}]";
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_STORE_CONFIG_METADATA.getRequestName());

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo(expectedMetadata);
    }

    @Test
    void shouldReturnArtifactStoreView() throws UnhandledRequestTypeException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_STORE_CONFIG_VIEW.getRequestName());

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        final Map<String, String> expectedTemplate = Collections.singletonMap("template", ResourceReader.read("/artifact-store.template.html"));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo(new Gson().toJson(expectedTemplate));
    }

    @Test
    void shouldValidateArtifactStoreConfig() throws UnhandledRequestTypeException, JSONException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_STORE_CONFIG_VALIDATE.getRequestName());
        when(request.requestBody()).thenReturn(GSON.toJson(Collections.singletonMap("Url", "httpd://foo/bar")));

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        final String expectedJson = "[{\"key\":\"Username\",\"message\":\"must be provided.\"},{\"key\":\"Password\",\"message\":\"must be provided.\"}]";
        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedJson,response.responseBody(),true);
    }

    @Test
    void shouldReturnPublishArtifactMetadata() throws UnhandledRequestTypeException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_PUBLISH_ARTIFACT_METADATA.getRequestName());

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        String expectedMetadata = "[{\"key\":\"Source\",\"metadata\":{\"required\":true,\"secure\":false}},{\"key\":\"Destination\",\"metadata\":{\"required\":true,\"secure\":false}}]";
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo(expectedMetadata);
    }

    @Test
    void shouldReturnPublishArtifactView() throws UnhandledRequestTypeException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_PUBLISH_ARTIFACT_VIEW.getRequestName());

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        final Map<String, String> expectedTemplate = Collections.singletonMap("template", ResourceReader.read("/publish-artifact.template.html"));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo(new Gson().toJson(expectedTemplate));
    }

    @Test
    void shouldValidateArtifactConfig() throws UnhandledRequestTypeException, JSONException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_PUBLISH_ARTIFACT_VALIDATE.getRequestName());
        when(request.requestBody()).thenReturn(GSON.toJson(Collections.singletonMap("Source", "abc")));

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        final String expectedJson = "[{\"key\":\"Destination\",\"message\":\"must be provided.\"}]";
        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedJson,response.responseBody(),true);

    }

    @Test
    void shouldReturnFetchArtifactMetadata() throws UnhandledRequestTypeException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_FETCH_ARTIFACT_METADATA.getRequestName());

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        String expectedMetadata = "[{\"key\":\"Path\",\"metadata\":{\"required\":true,\"secure\":false}}]";
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo(expectedMetadata);
    }

    @Test
    void shouldReturnFetchArtifactView() throws UnhandledRequestTypeException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer.REQUEST_FETCH_ARTIFACT_VIEW.getRequestName());

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        final Map<String, String> expectedTemplate = Collections.singletonMap("template", ResourceReader.read("/fetch-artifact.template.html"));
        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.responseBody()).isEqualTo(new Gson().toJson(expectedTemplate));
    }

    @Test
    void shouldValidateFetchArtifactConfig() throws UnhandledRequestTypeException, JSONException {
        final GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestName()).thenReturn(RequestFromServer. REQUEST_FETCH_ARTIFACT_VALIDATE.getRequestName());
        when(request.requestBody()).thenReturn("{}");

        final GoPluginApiResponse response = new DummyArtifactPlugin().handle(request);

        final String expectedJson = "[{\"key\":\"Path\",\"message\":\"must be provided.\"}]";
        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedJson,response.responseBody(),true);
    }
}