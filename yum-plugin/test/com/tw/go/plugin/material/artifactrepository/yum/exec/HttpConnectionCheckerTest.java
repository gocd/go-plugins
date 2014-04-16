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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class HttpConnectionCheckerTest {

    private HttpConnectionChecker checker;

    @Before
    public void setUp() throws Exception {
        checker = spy(new HttpConnectionChecker());
    }

    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        when(checker.getHttpClient()).thenReturn(httpClient);
        GetMethod getMethod = mock(GetMethod.class);
        when(checker.getGetMethod(Matchers.<String>any())).thenReturn(getMethod);
        when(httpClient.executeMethod(getMethod)).thenReturn(HttpStatus.SC_OK);

        checker.checkConnection("url", new Credentials(null, null));

        verify(httpClient).executeMethod(getMethod);
        verify(getMethod, never()).getStatusLine();
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenHttpClientReturnsAUnSuccessfulReturnCode() throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        GetMethod getMethod = mock(GetMethod.class);
        when(checker.getHttpClient()).thenReturn(httpClient);
        when(checker.getGetMethod(Matchers.<String>any())).thenReturn(getMethod);
        when(httpClient.executeMethod(getMethod)).thenReturn(HttpStatus.SC_UNAUTHORIZED);
        when(getMethod.getStatusLine()).thenReturn(new StatusLine("HTTP/1.1 401 Unauthorized"))    ;

        try {
            checker.checkConnection("url", new Credentials(null, null));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("HTTP/1.1 401 Unauthorized"));
        }
        verify(httpClient).executeMethod(getMethod);
        verify(getMethod).getStatusLine();
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenHttpClientThrowsIOException() throws IOException {
        HttpClient httpClient = mock(HttpClient.class);
        GetMethod getMethod = mock(GetMethod.class);
        when(checker.getHttpClient()).thenReturn(httpClient);
        when(checker.getGetMethod(Matchers.<String>any())).thenReturn(getMethod);
        when(httpClient.executeMethod(getMethod)).thenThrow(new IOException("some i/o error occurred"));

        try {
            checker.checkConnection("url", new Credentials(null, null));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("java.io.IOException: some i/o error occurred"));
        }
        verify(httpClient).executeMethod(getMethod);
        verify(getMethod, never()).getStatusLine();
    }
}
