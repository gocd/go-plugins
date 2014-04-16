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


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.tw.go.plugin.material.artifactrepository.yum.exec.config.YumRepositoryConfiguration;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class YumArtifactRepositoryMaterialTest {
    @Test
    public void shouldGetYumRepositoryConfig() {
        YumArtifactRepositoryMaterial repositoryMaterial = new YumArtifactRepositoryMaterial();
        PackageMaterialConfiguration repositoryConfiguration = repositoryMaterial.getConfig();
        assertThat(repositoryConfiguration, is(notNullValue()));
        assertThat(repositoryConfiguration, instanceOf(YumRepositoryConfiguration.class));
    }

    @Test
    public void shouldGetYumRepositoryPoller() {
        YumArtifactRepositoryMaterial repositoryMaterial = new YumArtifactRepositoryMaterial();
        PackageMaterialPoller poller = repositoryMaterial.getPoller();
        assertThat(poller, is(notNullValue()));
        assertThat(poller, instanceOf(YumRepositoryPoller.class));
    }
}
