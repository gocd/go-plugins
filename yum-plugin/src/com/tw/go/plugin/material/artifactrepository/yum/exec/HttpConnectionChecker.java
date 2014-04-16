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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;

import java.io.IOException;

public class HttpConnectionChecker implements ConnectionChecker {

    public void checkConnection(String url, Credentials credentials) {
        HttpClient client = getHttpClient();
        if (credentials.isComplete()) {
            org.apache.commons.httpclient.Credentials usernamePasswordCredentials = new UsernamePasswordCredentials(credentials.getUser(), credentials.getPassword());
            client.getState().setCredentials(AuthScope.ANY, usernamePasswordCredentials);
        }
        GetMethod method = getGetMethod(url);
        method.setFollowRedirects(false);
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode != HttpStatus.SC_OK) {
                throw new RuntimeException(method.getStatusLine().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    GetMethod getGetMethod(String url) {
        return new GetMethod(url);
    }

    HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, getSystemProperty("yum.repo.connection.timeout", 10 * 1000));
        client.getParams().setSoTimeout(getSystemProperty("yum.repo.socket.timeout", 5 * 60 * 1000));
        return client;
    }

    private int getSystemProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
