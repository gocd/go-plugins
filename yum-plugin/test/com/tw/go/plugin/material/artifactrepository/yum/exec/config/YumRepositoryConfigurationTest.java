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

package com.tw.go.plugin.material.artifactrepository.yum.exec.config;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.yum.exec.Constants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.material.artifactrepository.yum.exec.Constants.PACKAGE_SPEC;
import static com.tw.go.plugin.material.artifactrepository.yum.exec.Constants.REPO_URL;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class YumRepositoryConfigurationTest {
    private YumRepositoryConfiguration yumRepositoryConfiguration;

    @Before
    public void setUp() {
        yumRepositoryConfiguration = new YumRepositoryConfiguration();
    }

    @Test
    public void shouldGetRepositoryConfiguration() {
        RepositoryConfiguration configurations = yumRepositoryConfiguration.getRepositoryConfiguration();
        assertThat(configurations.get(REPO_URL), is(notNullValue()));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.SECURE), is(false));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.PART_OF_IDENTITY), is(true));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.REQUIRED), is(true));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.DISPLAY_NAME), is("Repository URL"));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.DISPLAY_ORDER), is(0));
        assertThat(configurations.get(Constants.USERNAME), is(notNullValue()));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.SECURE), is(false));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.PART_OF_IDENTITY), is(false));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.REQUIRED), is(false));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.DISPLAY_NAME), is("User"));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.DISPLAY_ORDER), is(1));
        assertThat(configurations.get(Constants.PASSWORD), is(notNullValue()));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.SECURE), is(true));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.PART_OF_IDENTITY), is(false));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.REQUIRED), is(false));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.DISPLAY_NAME), is("Password"));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.DISPLAY_ORDER), is(2));
    }

    @Test
    public void shouldGetPackageConfiguration() {
        PackageConfiguration configurations = yumRepositoryConfiguration.getPackageConfiguration();
        assertThat(configurations.get(PACKAGE_SPEC), is(notNullValue()));
        assertThat(configurations.get(Constants.PACKAGE_SPEC).getOption(Property.DISPLAY_NAME), is("Package Spec"));
        assertThat(configurations.get(Constants.PACKAGE_SPEC).getOption(Property.DISPLAY_ORDER), is(0));
    }

    @Test
    public void shouldCorrectlyCheckIfRepositoryConfigurationValid() {
        assertForRepositoryConfigurationErrors(new RepositoryConfiguration(), asList(new ValidationError(REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, null), asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, ""), asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, "incorrectUrl"), asList(new ValidationError(REPO_URL, "Invalid URL : incorrectUrl")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, "http://correct.com/url"), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldCorrectlyCheckIfPackageConfigurationValid() {
        assertForPackageConfigurationErrors(new PackageConfiguration(), asList(new ValidationError(PACKAGE_SPEC, "Package spec not specified")), false);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_SPEC, null), asList(new ValidationError(PACKAGE_SPEC, "Package spec is null")), false);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_SPEC, ""), asList(new ValidationError(PACKAGE_SPEC, "Package spec is empty")), false);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_SPEC, "go-age?nt-*"), new ArrayList<ValidationError>(), true);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_SPEC, "go-agent"), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldValidateConfig() throws Exception {
        ValidationResult validationResult = new ValidationResult();
        new YumRepositoryConfiguration().validate(new PackageConfiguration(), new RepositoryConfiguration(), validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors().contains(new ValidationError(REPO_URL, "Repository url not specified")), is(true));
        assertThat(validationResult.getErrors().contains(new ValidationError(PACKAGE_SPEC, "Package spec not specified")), is(true));
    }

    @Test
    public void shouldFailValidationIfSpuriousPropertiesAreConfigured() {
        ValidationResult validationResult = new ValidationResult();
        PackageConfiguration packageConfigurations = new PackageConfiguration();
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
        packageConfigurations.add(new PackageMaterialProperty("PACKAGE_SPEC", "foo"));
        packageConfigurations.add(new PackageMaterialProperty("foo1", "foo"));
        packageConfigurations.add(new PackageMaterialProperty("foo2", "foo"));
        repositoryConfiguration.add(new PackageMaterialProperty("bar1", "bar"));
        repositoryConfiguration.add(new PackageMaterialProperty("bar2", "bar"));
        repositoryConfiguration.add(new PackageMaterialProperty("REPO_URL", "http://asdsa"));
        new YumRepositoryConfiguration().validate(packageConfigurations, repositoryConfiguration, validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors().contains(new ValidationError("", "Unsupported key(s) found : bar1, bar2. Allowed key(s) are : REPO_URL, USERNAME, PASSWORD")), is(true));
        assertThat(validationResult.getErrors().contains(new ValidationError("", "Unsupported key(s) found : foo1, foo2. Allowed key(s) are : PACKAGE_SPEC")), is(true));
    }

    @Test
    public void shouldCorrectlyTestConnectionGivenCorrectConfiguration() {
        File sampleRepoDirectory = new File("test/repos/samplerepo");

        RepositoryConfiguration repositoryConfigurations = repoConfigurations(REPO_URL, "file://" + sampleRepoDirectory.getAbsolutePath());
        PackageConfiguration packageConfigurations = packageConfigurations(PACKAGE_SPEC, "go-agent");

        try {
            yumRepositoryConfiguration.testConnection(packageConfigurations, repositoryConfigurations);
        } catch (Exception e) {
            fail("");
        }
    }

    @Test
    public void shouldCorrectlyTestConnectionGivenIncorrectConfiguration() {
        RepositoryConfiguration repositoryConfigurations = repoConfigurations(REPO_URL, "file://junk-repo");
        PackageConfiguration packageConfigurations = packageConfigurations(PACKAGE_SPEC, "go-agent");

        try {
            yumRepositoryConfiguration.testConnection(packageConfigurations, repositoryConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Test Connection failed."));
        }
    }

    private void assertForRepositoryConfigurationErrors(RepositoryConfiguration repositoryConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        ValidationResult validationResult = yumRepositoryConfiguration.isRepositoryConfigurationValid(repositoryConfigurations);
        assertThat(validationResult.isSuccessful(), is(expectedValidationResult));
        assertThat(validationResult.getErrors().size(), is(expectedErrors.size()));
        assertThat(validationResult.getErrors().containsAll(expectedErrors), is(true));
    }

    private void assertForPackageConfigurationErrors(PackageConfiguration packageConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        ValidationResult validationResult = yumRepositoryConfiguration.isPackageConfigurationValid(packageConfigurations, new RepositoryConfiguration());
        assertThat(validationResult.isSuccessful(), is(expectedValidationResult));
        assertThat(validationResult.getErrors().size(), is(expectedErrors.size()));
        assertThat(validationResult.getErrors().containsAll(expectedErrors), is(true));
    }

    private PackageConfiguration packageConfigurations(String key, String value) {
        PackageConfiguration configurations = new PackageConfiguration();
        configurations.add(new PackageMaterialProperty(key, value));
        return configurations;
    }

    private RepositoryConfiguration repoConfigurations(String key, String value) {
        RepositoryConfiguration configurations = new RepositoryConfiguration();
        configurations.add(new PackageMaterialProperty(key, value));
        return configurations;
    }
}
