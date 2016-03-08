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

import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.*;

import java.io.IOException;

public class HttpConnectionChecker implements ConnectionChecker {

    public void checkConnection(String url, Credentials credentials) {
        try (CloseableHttpClient client = getHttpClient(credentials)) {
            HttpGet method = getGetMethod(url);
            try (CloseableHttpResponse response = client.execute(method)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException(response.getStatusLine().toString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    HttpGet getGetMethod(String url) {
        return new HttpGet(url);
    }

    CloseableHttpClient getHttpClient(Credentials credentials) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        if (credentials.isComplete()) {
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials.getUser(), credentials.getPassword()));
        }

        RequestConfig requestConfig = RequestConfig.custom().
                setConnectTimeout(getSystemProperty("yum.repo.connection.timeout", 10 * 1000)).
                setSocketTimeout(getSystemProperty("yum.repo.socket.timeout", 5 * 60 * 1000)).
                setAuthenticationEnabled(true).
                setMaxRedirects(10).
                build();

        return HttpClients.custom().
                setRedirectStrategy(new DefaultRedirectStrategy()).
                setDefaultCredentialsProvider(credentialsProvider).
                setDefaultRequestConfig(requestConfig).
                setTargetAuthenticationStrategy(new TargetAuthenticationStrategy()).
                build();

//        HttpClient client = new HttpClient();
//        client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, getSystemProperty("yum.repo.connection.timeout", 10 * 1000));
//        client.getParams().setSoTimeout(getSystemProperty("yum.repo.socket.timeout", 5 * 60 * 1000));
//        return client;
    }

    private int getSystemProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
