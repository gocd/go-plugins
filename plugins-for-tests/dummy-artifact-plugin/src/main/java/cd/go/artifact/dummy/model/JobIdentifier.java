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

package cd.go.artifact.dummy.model;

import java.util.Map;

public class JobIdentifier {
    private String pipeline;
    private String pipelineCounter;
    private String stage;
    private String stageCounter;
    private String job;

    public JobIdentifier() {
    }

    public JobIdentifier(Map<String, String> environmentVariables) {
        pipeline = environmentVariables.get("GO_PIPELINE_NAME");
        pipelineCounter = environmentVariables.get("GO_PIPELINE_COUNTER");
        stage = environmentVariables.get("GO_STAGE_NAME");
        stageCounter = environmentVariables.get("GO_STAGE_COUNTER");
        job = environmentVariables.get("GO_JOB_NAME");
    }

    public String getPipeline() {
        return pipeline;
    }

    public String getPipelineCounter() {
        return pipelineCounter;
    }

    public String getStage() {
        return stage;
    }

    public String getStageCounter() {
        return stageCounter;
    }

    public String getJob() {
        return job;
    }
}
