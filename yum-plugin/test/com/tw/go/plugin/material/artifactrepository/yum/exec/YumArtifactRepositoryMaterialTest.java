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
package com.tw.go.plugin.material.artifactrepository.yum.exec;

import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.CheckConnectionResultMessage;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageRevisionMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static com.tw.go.plugin.common.util.JsonUtil.fromJsonString;
import static com.tw.go.plugin.material.artifactrepository.yum.exec.YumArtifactRepositoryMaterial.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class YumArtifactRepositoryMaterialTest {

    private YumArtifactRepositoryMaterial material;
    private File sampleRepoDirectory;
    private String repoUrl;

    @Before
    public void setUp() throws Exception {
        material = new YumArtifactRepositoryMaterial();
        RepoqueryCacheCleaner.performCleanup();
        sampleRepoDirectory = new File("test/repos/samplerepo");
        repoUrl = "file://" + sampleRepoDirectory.getAbsolutePath();
    }

    @Test
    public void shouldReturnResponseForRepositoryConfiguration() throws Exception {
        GoPluginApiResponse response = material.handle(new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_REPOSITORY_CONFIGURATION));
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is("{\"REPO_URL\":{\"display-name\":\"Repository URL\",\"display-order\":\"0\"}," +
                "\"USERNAME\":{\"part-of-identity\":false,\"required\":false,\"display-name\":\"User\",\"display-order\":\"1\"}," +
                "\"PASSWORD\":{\"secure\":true,\"part-of-identity\":false,\"required\":false,\"display-name\":\"Password\",\"display-order\":\"2\"}" +
                "}"));
    }

    @Test
    public void shouldReturnResponseForPackageConfiguration() throws Exception {
        GoPluginApiResponse response = material.handle(new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_PACKAGE_CONFIGURATION));
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is("{\"PACKAGE_SPEC\":{\"display-name\":\"Package Spec\",\"display-order\":\"0\"}}"));
    }

    @Test
    public void shouldReturnSuccessForValidateRepositoryConfiguration() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_VALIDATE_REPOSITORY_CONFIGURATION);
        request.setRequestBody("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"http://localhost.com\"}}}");
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is(""));
    }

    @Test
    public void shouldReturnFailureForValidateRepositoryConfiguration() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_VALIDATE_REPOSITORY_CONFIGURATION);
        request.setRequestBody("{\"repository-configuration\":{\"RANDOM\":{\"value\":\"value\"}}}");
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is("[{\"key\":\"\",\"message\":\"Unsupported key(s) found : RANDOM. Allowed key(s) are : REPO_URL, USERNAME, PASSWORD\"},{\"key\":\"REPO_URL\",\"message\":\"Repository url not specified\"}]"));
    }

    @Test
    public void shouldReturnSuccessForValidatePackageConfiguration() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_VALIDATE_PACKAGE_CONFIGURATION);
        request.setRequestBody("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"http://localhost.com\"}},\"package-configuration\":{\"PACKAGE_SPEC\":{\"value\":\"go-agent\"}}}");
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is(""));
    }

    @Test
    public void shouldReturnFailureForValidatePackageConfiguration() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_VALIDATE_PACKAGE_CONFIGURATION);
        request.setRequestBody("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"http://localhost.com\"}},\"package-configuration\":{\"RANDOM\":{\"value\":\"go-agent\"}}}");
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), is("[{\"key\":\"\",\"message\":\"Unsupported key(s) found : RANDOM. Allowed key(s) are : PACKAGE_SPEC\"},{\"key\":\"PACKAGE_SPEC\",\"message\":\"Package spec not specified\"}]"));
    }


    @Test
    public void shouldReturnSuccessForCheckRepositoryConnection() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_CHECK_REPOSITORY_CONNECTION);
        request.setRequestBody(String.format("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"%s\"}}}", repoUrl));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        CheckConnectionResultMessage result = fromJsonString(response.responseBody(), CheckConnectionResultMessage.class);
        assertThat(result.success(), is(true));
        assertThat(result.getMessages().isEmpty(), is(false));
    }

    @Test
    public void shouldReturnFailureForCheckRepositoryConnection() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_CHECK_REPOSITORY_CONNECTION);
        request.setRequestBody(String.format("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"%s\"}}}", repoUrl + "/random"));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        CheckConnectionResultMessage result = fromJsonString(response.responseBody(), CheckConnectionResultMessage.class);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().isEmpty(), is(false));
    }

    @Test
    public void shouldReturnSuccessForCheckPackageConnection() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_CHECK_PACKAGE_CONNECTION);
        request.setRequestBody(String.format("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"%s\"}},\"package-configuration\":{\"PACKAGE_SPEC\":{\"value\":\"go-agent\"}}}", repoUrl));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        CheckConnectionResultMessage result = fromJsonString(response.responseBody(), CheckConnectionResultMessage.class);
        assertThat(result.success(), is(true));
        assertThat(result.getMessages().isEmpty(), is(false));
    }

    @Test
    public void shouldReturnFailureForCheckPackageConnection() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_CHECK_PACKAGE_CONNECTION);
        request.setRequestBody(String.format("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"%s\"}},\"package-configuration\":{\"PACKAGE_SPEC\":{\"value\":\"incorrect\"}}}", repoUrl));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        CheckConnectionResultMessage result = fromJsonString(response.responseBody(), CheckConnectionResultMessage.class);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().isEmpty(), is(false));
    }

    @Test
    public void shouldReturnLatestPackageRevision() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_LATEST_PACKAGE_REVISION);
        request.setRequestBody(String.format("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"%s\"}},\"package-configuration\":{\"PACKAGE_SPEC\":{\"value\":\"go-agent\"}}}", repoUrl));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        PackageRevisionMessage packageRevision = fromJsonString(response.responseBody(), PackageRevisionMessage.class);
        assertThat(packageRevision.getData().isEmpty(),is(false));
        assertThat(response.responseBody().contains("\"revision\":\"go-agent-13.1.1-16714.noarch\""), is(true));
        assertThat(response.responseBody().contains("\"timestamp\":\"2013-04-04T11:14:18.000Z\""), is(true));
    }

    @Test
    public void shouldReturnLatestPackageRevisionSince() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_LATEST_PACKAGE_REVISION_SINCE);
        request.setRequestBody(String.format("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"%s\"}}," +
                "\"package-configuration\":{\"PACKAGE_SPEC\":{\"value\":\"go-agent\"}}," +
                "\"previous-revision\":{\"revision\":\"go-agent-13.1.0-16714.noarch\",\"timestamp\":\"2013-04-03T11:14:18.000Z\",\"data\":{\"data-key-one\":\"data-value-one\",\"data-key-two\":\"data-value-two\"}}}", repoUrl));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        PackageRevisionMessage packageRevision = fromJsonString(response.responseBody(), PackageRevisionMessage.class);
        assertThat(packageRevision.getData().isEmpty(),is(false));
        assertThat(response.responseBody().contains("\"revision\":\"go-agent-13.1.1-16714.noarch\""), is(true));
        assertThat(response.responseBody().contains("\"timestamp\":\"2013-04-04T11:14:18.000Z\""), is(true));
    }

    @Test
    public void shouldReturnNullLatestPackageRevisionSince() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest(EXTENSION, "1.0", REQUEST_LATEST_PACKAGE_REVISION_SINCE);
        request.setRequestBody(String.format("{\"repository-configuration\":{\"REPO_URL\":{\"value\":\"%s\"}}," +
                "\"package-configuration\":{\"PACKAGE_SPEC\":{\"value\":\"go-agent\"}}," +
                "\"previous-revision\":{\"revision\":\"go-agent-13.1.1-16714.noarch\",\"timestamp\":\"2013-04-04T11:14:18.000Z\",\"data\":{\"data-key-one\":\"data-value-one\",\"data-key-two\":\"data-value-two\"}}}", repoUrl));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE));
        assertThat(response.responseBody(), nullValue());
    }


    @Test
    public void shouldReturnUnSuccessfulResponseWhenHandlerNotFondForRequest() throws Exception {
        assertThat(material.handle(new DefaultGoPluginApiRequest(EXTENSION, "1.0", "invalid")).responseCode(), is(400));
        assertThat(material.handle(new DefaultGoPluginApiRequest(EXTENSION, "1.0", null)).responseCode(), is(400));
        assertThat(material.handle(new DefaultGoPluginApiRequest(EXTENSION, "1.0", "")).responseCode(), is(400));
    }

    @Test
    public void shouldReturnUnSuccessfulResponseOnException() throws Exception {
        DefaultGoPluginApiRequest request = new DefaultGoPluginApiRequest("package-repository", "1.0", "repository-configuration");
        final MessageHandler messageHandler = mock(MessageHandler.class);
        material = new YumArtifactRepositoryMaterial() {
            @Override
            MessageHandler repositoryConfigurationsMessageHandler() {
                return messageHandler;
            }
        };
        when(messageHandler.handle(request)).thenThrow(new RuntimeException("failed-for-some-reason"));
        GoPluginApiResponse response = material.handle(request);
        assertThat(response.responseCode(), is(500));
        assertThat(response.responseBody(), is("failed-for-some-reason"));
    }

    @Test
    public void shouldReturnPluginIdentifierForPackageRepository() throws Exception {
        GoPluginIdentifier pluginIdentifier = material.pluginIdentifier();
        assertThat(pluginIdentifier.getExtension(), is(EXTENSION));
        assertThat(pluginIdentifier.getSupportedExtensionVersions(), is(asList("1.0")));
    }

    @After
    public void tearDown() throws Exception {
        RepoqueryCacheCleaner.performCleanup();
    }
}