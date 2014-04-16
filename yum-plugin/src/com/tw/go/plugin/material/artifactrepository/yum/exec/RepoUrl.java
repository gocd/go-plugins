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
import com.tw.go.plugin.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RepoUrl {
    private final String url;
    private Credentials credentials;
    private static HashMap<String, ConnectionChecker> map = new HashMap<String, ConnectionChecker>();
    private static FileBasedConnectionChecker fileBasedConnectionChecker = new FileBasedConnectionChecker();
    private static HttpConnectionChecker httpConnectionChecker = new HttpConnectionChecker();

    static {
        map.put("file", fileBasedConnectionChecker);
        map.put("http", httpConnectionChecker);
        map.put("https", httpConnectionChecker);
    }

    public RepoUrl(String url, String user, String password) {
        this.url = url;
        this.credentials = new Credentials(user, password);
    }

    public void validate(ValidationResult validationResult) {
        try {
            if (StringUtil.isBlank(url)) {
                validationResult.addError(new ValidationError(Constants.REPO_URL, "Repository url is empty"));
                return;
            }
            URL validatedUrl = new URL(this.url);
            if (!map.containsKey(validatedUrl.getProtocol())) {
                validationResult.addError(new ValidationError(Constants.REPO_URL, "Invalid URL: Only 'file', 'http' and 'https' protocols are supported."));
            }

            if (StringUtil.isNotBlank(validatedUrl.getUserInfo())) {
                validationResult.addError(new ValidationError(Constants.REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
            }
            if (credentials.isPresent()) {
                if(validatedUrl.getProtocol().equals("file")){
                    validationResult.addError(new ValidationError(Constants.REPO_URL, "File protocol does not support username and/or password."));
                } else {
                    credentials.validate(validationResult);
                }
            }
        } catch (MalformedURLException e) {
            validationResult.addError(new ValidationError(Constants.REPO_URL, "Invalid URL : " + url));
        }
    }

    ConnectionChecker getChecker() {
        try {
            return map.get(new URL(url).getProtocol());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + e);
        }
    }


    public String getUrlWithBasicAuth() {
        String localUrl = this.url;
        try {
            new URL(localUrl);
            if (credentials.isComplete()) {
                String[] split = localUrl.split("//");
                if (split.length != 2) throw new RuntimeException(String.format("Invalid uri format %s", this.url));
                localUrl = split[0] + "//" + credentials.getUserInfo() + "@" + split[1];
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return localUrl;
    }

    public void checkConnection() {
        getChecker().checkConnection(getRepoMetadataUrl(), credentials);
    }

    public String getRepoMetadataUrl() {
        Pattern pattern = Pattern.compile("(.*?)(/+)$");
        String urlStrippedOfTrailingSlashes = pattern.matcher(url).replaceAll("$1");
        return urlStrippedOfTrailingSlashes + "/repodata/repomd.xml";
    }

    public String forDisplay() {
        return url;
    }
}
