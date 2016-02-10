/*
 * Copyright 2016 ThoughtWorks, Inc.
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

package com.tw.go.plugin.material.artifactrepository.yum.exec;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class HttpConnectionCheckerTest {

    private HttpConnectionChecker checker;
    private MockWebServer webServer;

    @Before
    public void setUp() throws Exception {
        webServer = new MockWebServer();
        checker = new HttpConnectionChecker();
    }

    @After
    public void tearDown() throws Exception {
        webServer.shutdown();
    }

    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() throws Exception {
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(""));
        HttpUrl url = webServer.url("/repodata/repomd.xml");

        checker.checkConnection(url.toString(), new Credentials(null, null));

        RecordedRequest recordedRequest = webServer.takeRequest();
        assertEquals("/repodata/repomd.xml", recordedRequest.getPath());
        assertEquals(1, webServer.getRequestCount());
    }

    @Test
    public void shouldPerformBasicAuthUsingChallengeResponseAuth() throws Exception {
        webServer.enqueue(new MockResponse().setResponseCode(401).setHeader("WWW-Authenticate", "Basic realm=\"YumRepo\""));
        webServer.enqueue(new MockResponse().setResponseCode(200).setBody(""));

        HttpUrl url = webServer.url("/repodata/repomd.xml");

        checker.checkConnection(url.toString(), new Credentials("foo", "bar"));
        assertEquals(2, webServer.getRequestCount());

        RecordedRequest recordedRequest = webServer.takeRequest();
        assertEquals("/repodata/repomd.xml", recordedRequest.getPath());
        assertThat(recordedRequest.getHeaders().get("Authorization"), is((String) null));

        recordedRequest = webServer.takeRequest();
        assertEquals("/repodata/repomd.xml", recordedRequest.getPath());
        assertThat(recordedRequest.getHeaders().get("Authorization"), is("Basic Zm9vOmJhcg=="));
    }


    @Test
    public void shouldFailCheckConnectionToTheRepoWhenHttpClientReturnsAUnSuccessfulReturnCode() throws IOException {
        webServer.enqueue(new MockResponse().setResponseCode(500));
        HttpUrl url = webServer.url("/repodata/repomd.xml");

        try {
            checker.checkConnection(url.toString(), new Credentials(null, null));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("HTTP/1.1 500 Server Error"));
        }
        assertEquals(1, webServer.getRequestCount());
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenHttpClientThrowsIOException() throws IOException {
        try {
            checker.checkConnection("https://localhost:11111", new Credentials(null, null));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("Connection refused"));
        }
    }
}
