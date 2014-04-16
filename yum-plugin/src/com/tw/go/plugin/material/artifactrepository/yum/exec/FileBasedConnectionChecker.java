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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileBasedConnectionChecker implements ConnectionChecker {
    public void checkConnection(String givenUrl, Credentials credentials) {
        if (credentials.isComplete()) {
            throw new RuntimeException("File protocol does not support username and/or password.");
        }
        try {
            URL url = new URL(givenUrl);
            if (!new File(url.getPath()).exists()) {
                throw new RuntimeException("Invalid file path.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
