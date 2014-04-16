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

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import org.junit.Test;
import org.mockito.Matchers;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.material.artifactrepository.yum.exec.Constants.REPO_URL;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RepoUrlTest {
    @Test
    public void shouldCorrectlyCheckIfRepositoryConfigurationValid() {
        assertRepositoryUrlValidation("", null, null, asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertRepositoryUrlValidation(null, null, null, asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertRepositoryUrlValidation("  ", null, null, asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertRepositoryUrlValidation("h://localhost", null, null, asList(new ValidationError(REPO_URL, "Invalid URL : h://localhost")), false);
        assertRepositoryUrlValidation("ftp:///foo.bar", null, null, asList(new ValidationError(REPO_URL, "Invalid URL: Only 'file', 'http' and 'https' protocols are supported.")), false);
        assertRepositoryUrlValidation("incorrectUrl", null, null, asList(new ValidationError(REPO_URL, "Invalid URL : incorrectUrl")), false);
        assertRepositoryUrlValidation("http://user:password@localhost", null, null, asList(new ValidationError(REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys.")), false);
        assertRepositoryUrlValidation("http://correct.com/url", null, null, new ArrayList<ValidationError>(), true);
        assertRepositoryUrlValidation("file:///foo.bar", null, null, new ArrayList<ValidationError>(), true);
        assertRepositoryUrlValidation("file:///foo.bar", "user", "password", asList(new ValidationError(REPO_URL, "File protocol does not support username and/or password.")), false);
    }

    @Test
    public void shouldThrowUpWhenFileProtocolAndCredentialsAreUsed() throws Exception {
        RepoUrl repoUrl = new RepoUrl("file://foo.bar", null, "password");
        ValidationResult errors = new ValidationResult();

        repoUrl.validate(errors);

        assertThat(errors.isSuccessful(), is(false));
        assertThat(errors.getErrors().size(), is(1));
        assertThat(errors.getErrors().get(0).getMessage(), is("File protocol does not support username and/or password."));
    }

    @Test
    public void shouldReturnURLWithBasicAuth() {
        RepoUrl repoUrl = new RepoUrl("http://localhost", "user", "password");
        assertThat(repoUrl.getUrlWithBasicAuth(), is("http://user:password@localhost"));
    }

    @Test
    public void shouldReturnTheRightConnectionCheckerBasedOnUrlScheme() {
        ConnectionChecker checker = new RepoUrl("http://foobar.com", null, null).getChecker();
        assertThat(checker instanceof HttpConnectionChecker, is(true));

        checker = new RepoUrl("https://foobar.com", null, null).getChecker();
        assertThat(checker instanceof HttpConnectionChecker, is(true));

        checker = new RepoUrl("file://foo/bar", null, null).getChecker();
        assertThat(checker instanceof FileBasedConnectionChecker, is(true));
    }

    @Test
    public void shouldThrowExceptionIfURIIsInvalid_checkConnection() {
        try {
            new RepoUrl("://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("Invalid URL: java.net.MalformedURLException: no protocol: ://foobar.com"));
        }
    }

    @Test
    public void shouldThrowExceptionIfSchemeIsInvalid_checkConnection() {
        try {
            new RepoUrl("httph://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid URL: java.net.MalformedURLException: unknown protocol: httph"));
        }
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenHttpUrlIsNotReachable() {
        try {
            RepoUrl repoUrl = spy(new RepoUrl("url", null, null));
            ConnectionChecker connectionChecker = mock(ConnectionChecker.class);
            doThrow(new RuntimeException("Unreachable url")).when(connectionChecker).checkConnection(Matchers.<String>any(), Matchers.<Credentials>any());
            doReturn(connectionChecker).when(repoUrl).getChecker();
            repoUrl.checkConnection();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Unreachable url"));
        }
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenRepoFileSystemPathIsNotReachable() {
        try {
            new RepoUrl("file:///foo/bar", null, null).checkConnection();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid file path."));
        }
    }

    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() {
        RepoUrl repoUrl = spy(new RepoUrl("url", null, null));
        ConnectionChecker connectionChecker = mock(ConnectionChecker.class);
        doReturn(connectionChecker).when(repoUrl).getChecker();

        repoUrl.checkConnection();

        verify(connectionChecker).checkConnection(Matchers.<String>any(), Matchers.<Credentials>any());
    }

    @Test
    public void shouldGetUrlForDisplay() throws Exception {
        assertThat(new RepoUrl("file:///foo/bar", null, null).forDisplay(), is("file:///foo/bar"));
    }

    @Test
    public void shouldGetRepoMetadataUrl() throws Exception {
        assertThat(new RepoUrl("file:///foo/bar", null, null).getRepoMetadataUrl(), is("file:///foo/bar/repodata/repomd.xml"));
        assertThat(new RepoUrl("file:///foo/bar/", null, null).getRepoMetadataUrl(), is("file:///foo/bar/repodata/repomd.xml"));
        assertThat(new RepoUrl("file:///foo/bar//", null, null).getRepoMetadataUrl(), is("file:///foo/bar/repodata/repomd.xml"));
    }

    private void assertRepositoryUrlValidation(String url, String username, String password, List<ValidationError> expectedErrors, boolean isSuccessful) {
        ValidationResult errors = new ValidationResult();
        new RepoUrl(url, username, password).validate(errors);
        assertThat(errors.isSuccessful(), is(isSuccessful));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }
}
