/*
 * Copyright 2021 Thoughtworks, Inc.
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

package cd.go.artifact.dummy.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class PublishMetadata {
    @SerializedName("filename")
    private String filename;
    @SerializedName("jobIdentifier")
    private JobIdentifier jobIdentifier;

    public PublishMetadata() {
    }

    public PublishMetadata(String filename, JobIdentifier jobIdentifier) {
        this.filename = filename;
        this.jobIdentifier = jobIdentifier;
    }


    public JobIdentifier getJobIdentifier() {
        return jobIdentifier;
    }
}
