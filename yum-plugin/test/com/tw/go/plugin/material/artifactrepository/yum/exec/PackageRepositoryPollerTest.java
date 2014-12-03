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

import com.tw.go.plugin.material.artifactrepository.yum.exec.message.CheckConnectionResultMessage;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageMaterialProperties;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageMaterialProperty;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageRevisionMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class PackageRepositoryPollerTest {
    private PackageMaterialProperties repositoryConfiguration;
    private PackageMaterialProperties packageConfiguration;
    private File sampleRepoDirectory;
    private String repoUrl;
    private PackageRepositoryPoller poller;
    private PackageRepositoryConfigurationProvider configurationProvider;

    @Before
    public void setup() throws IOException {
        RepoqueryCacheCleaner.performCleanup();
        sampleRepoDirectory = new File("test/repos/samplerepo");
        repoUrl = "file://" + sampleRepoDirectory.getAbsolutePath();

        repositoryConfiguration = new PackageMaterialProperties();
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue(repoUrl));

        packageConfiguration = new PackageMaterialProperties();
        packageConfiguration.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("go-agent"));

        configurationProvider = new PackageRepositoryConfigurationProvider();
        poller = new PackageRepositoryPoller(configurationProvider);
    }

    @Test
    public void shouldGetLatestModificationGivenPackageAndRepoConfigurations_getLatestRevision() {
        PackageRevisionMessage latestRevision = poller.getLatestRevision(packageConfiguration, repositoryConfiguration);
        assertThat(latestRevision, is(new PackageRevisionMessage("go-agent-13.1.1-16714.noarch", new Date(fromEpochTime(1365054258L)), null, null, null)));
        assertThat(latestRevision.getDataFor("LOCATION"), is("file://" + sampleRepoDirectory.getAbsolutePath() + "/go-agent-13.1.1-16714.noarch.rpm"));
    }

    @Test
    public void shouldThrowExceptionWhileGettingLatestRevisionIfCheckConnectionFails_getLatestRevision() {
        repositoryConfiguration = new PackageMaterialProperties();
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("file://foo/bar"));
        try {
            poller.getLatestRevision(packageConfiguration, repositoryConfiguration);
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Invalid file path."));
        }
    }

    @Test
    public void shouldGetTheRightLocationForAnyPackage_getLatestRevision() {
        PackageMaterialProperties ppc = new PackageMaterialProperties();
        ppc.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("php"));
        PackageRevisionMessage latestRevision = poller.getLatestRevision(ppc, repositoryConfiguration);
        assertThat(latestRevision, is(new PackageRevisionMessage("php-0-0.noarch", new Date(fromEpochTime(1365053593)), null, null, null)));
        assertThat(latestRevision.getDataFor("LOCATION"), is("file://" + sampleRepoDirectory.getAbsolutePath() + "/innerFolder/php-0-0.noarch.rpm"));
    }

    @Test
    public void shouldThrowExceptionGivenNonExistingRepo_getLatestRevision() {
        repositoryConfiguration = new PackageMaterialProperties();
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("file://junk-repo"));
        packageConfiguration = new PackageMaterialProperties();
        packageConfiguration.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("junk-artifact"));
        try {
            poller.getLatestRevision(packageConfiguration, repositoryConfiguration);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage().startsWith("Invalid file path."), is(true));
        }
    }

    @Test
    public void shouldThrowExceptionGivenNonExistingPackageInExistingRepo_getLatestRevision() {
        packageConfiguration = new PackageMaterialProperties();
        packageConfiguration.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("junk-artifact"));

        try {
            poller.getLatestRevision(packageConfiguration, repositoryConfiguration);
            fail("");
        } catch (RuntimeException e) {
            String expectedMessage = String.format("Error while querying repository with path '%s' and package spec '%s'.", repositoryConfiguration.getProperty(Constants.REPO_URL).value(), "junk-artifact");
            assertThat(e.getMessage().startsWith(expectedMessage), is((true)));
        }
    }

    @Test
    public void shouldThrowExceptionGivenEmptyRepo_getLatestRevision() {
        repositoryConfiguration = new PackageMaterialProperties();
        File emptyRepo = new File("test/repos/emptyrepo");
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("file://" + emptyRepo.getAbsolutePath()));
        packageConfiguration = new PackageMaterialProperties();
        packageConfiguration.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("junk-artifact"));
        try {
            poller.getLatestRevision(packageConfiguration, repositoryConfiguration);
            fail("");
        } catch (RuntimeException e) {
            String expectedMessage = String.format("Error while querying repository with path '%s' and package spec '%s'.", repositoryConfiguration.getProperty(Constants.REPO_URL).value(), "junk-artifact");
            assertThat(e.getMessage().startsWith(expectedMessage), is((true)));
        }
    }

    @Test
    public void shouldPerformRepositoryConfigurationBeforeModificationCheck_getLatestRevision() {
        packageConfiguration = new PackageMaterialProperties();
        packageConfiguration.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("junk-artifact"));
        try {
            poller.getLatestRevision(packageConfiguration, new PackageMaterialProperties());
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Repository url not specified")));
        }
    }

    @Test
    public void shouldPerformPackageConfigurationBeforeModificationCheck() {
        repositoryConfiguration = new PackageMaterialProperties();
        File emptyRepo = new File("test/repos/emptyrepo");
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("file://" + emptyRepo.getAbsolutePath()));
        try {
            poller.getLatestRevision(new PackageMaterialProperties(), repositoryConfiguration);
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Package spec not specified")));
        }
    }

    @Test
    public void testShouldConcatenateErrorsWhenModificationCheckFails() {
        try {
            poller.getLatestRevision(new PackageMaterialProperties(), new PackageMaterialProperties());
            fail("should have thrown exception");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Repository url not specified; Package spec not specified")));
        }
    }

    @Test
    public void shouldGetLatestModificationSinceGivenPackageAndRepoConfigurationsAndPreviouslyKnownRevision() {
        PackageRevisionMessage previousPackageRevision = new PackageRevisionMessage("symlinks-1.2-24.2.2.i386", new Date(fromEpochTime(1263710418L)), null, null, null);
        PackageRevisionMessage latestRevision = poller.getLatestRevisionSince(packageConfiguration, repositoryConfiguration, previousPackageRevision);
        assertThat(latestRevision, is(new PackageRevisionMessage("go-agent-13.1.1-16714.noarch", new Date(fromEpochTime(1365054258L)), null, null, null)));
    }

    @Test
    public void shouldReturnNullGivenPackageAndRepoConfigurationsAndPreviouslyKnownRevision() {
        PackageRevisionMessage packageRevisionMessage = new PackageRevisionMessage("go-agent-13.1.1-16714-noarch", new Date(fromEpochTime(1365054258L)), null, null, null);
        PackageRevisionMessage latestRevision = poller.getLatestRevisionSince(packageConfiguration, repositoryConfiguration, packageRevisionMessage);
        assertThat(latestRevision, is(nullValue()));
    }

    @Test
    public void shouldReturnNullWhenPreviouslyKnownPackageRevisionIsSameAsCurrent() {
        PackageRepositoryPoller spy = spy(poller);
        PackageRevisionMessage packageRevision = new PackageRevisionMessage("go-agent-13.1.1-16714-noarch", new Date(fromEpochTime(1365054258L)), null, null, null);
        when(spy.getLatestRevision(packageConfiguration, repositoryConfiguration)).thenReturn(packageRevision);
        PackageRevisionMessage latestRevision = poller.getLatestRevisionSince(packageConfiguration, repositoryConfiguration, new PackageRevisionMessage("go-agent-13.1.1-16714-noarch", new Date(fromEpochTime(1365054258L)), null, null, null));
        assertThat(latestRevision, is(nullValue()));
    }

    @Test
    public void shouldThrowExceptionIfCredentialsHaveBeenProvidedAlongWithFileProtocol() {
        repositoryConfiguration.addPackageMaterialProperty(Constants.USERNAME, new PackageMaterialProperty().withValue("loser"));
        repositoryConfiguration.addPackageMaterialProperty(Constants.PASSWORD, new PackageMaterialProperty().withValue("pwd"));
        try {
            poller.getLatestRevision(packageConfiguration, repositoryConfiguration);
            fail("Should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("File protocol does not support username and/or password."));
        }
    }

    @Test
    public void shouldCheckRepoConnection() throws Exception {
        CheckConnectionResultMessage checkConnectionResultMessage = poller.checkConnectionToRepository(repositoryConfiguration);
        assertThat(checkConnectionResultMessage.success(), is(true));
        assertThat(checkConnectionResultMessage.getMessages().size(), is(1));
        assertThat(checkConnectionResultMessage.getMessages().get(0), is(String.format("Successfully accessed repository metadata at %s", repoUrl + "/repodata/repomd.xml")));
    }

    @Test
    public void shouldReturnErrorsWhenConnectionToRepoFails() throws Exception {
        repositoryConfiguration = new PackageMaterialProperties();
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("file://invalid_path"));

        CheckConnectionResultMessage result = poller.checkConnectionToRepository(repositoryConfiguration);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().get(0), is("Could not access file - file://invalid_path/repodata/repomd.xml. Invalid file path."));
    }

    @Test
    public void shouldPerformRepoValidationsBeforeCheckConnection() throws Exception {
        repositoryConfiguration = new PackageMaterialProperties();
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("ftp://username:password@invalid_path"));

        CheckConnectionResultMessage result = poller.checkConnectionToRepository(repositoryConfiguration);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().size(), is(2));
        assertThat(result.getMessages().get(0), is("Invalid URL: Only 'file', 'http' and 'https' protocols are supported."));
        assertThat(result.getMessages().get(1), is("User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
    }

    @Test
    public void shouldCheckConnectionToPackageAndRespondWithLatestPackageFound() {
        CheckConnectionResultMessage result = poller.checkConnectionToPackage(packageConfiguration, repositoryConfiguration);
        assertThat(result.success(), is(true));
        assertThat(result.getMessages().get(0), is("Found package 'go-agent-13.1.1-16714.noarch'."));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfPackageIsNotFound() {
        packageConfiguration = new PackageMaterialProperties();
        packageConfiguration.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("go-a"));
        CheckConnectionResultMessage result = poller.checkConnectionToPackage(packageConfiguration, repositoryConfiguration);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().get(0), is("Could not find any package that matched 'go-a'."));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfMultiplePackageIsFound() {
        packageConfiguration = new PackageMaterialProperties();
        packageConfiguration.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("go*"));
        CheckConnectionResultMessage result = poller.checkConnectionToPackage(packageConfiguration, repositoryConfiguration);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().get(0).startsWith("Given Package Spec (go*) resolves to more than one file on the repository: "), is(true));
        assertThat(result.getMessages().get(0).contains("go-agent-13.1.1-16714.noarch.rpm"), is(true));
        assertThat(result.getMessages().get(0).contains("go-server-13.1.1-16714.noarch.rpm"), is(true));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfRepositoryIsNotReachable() {
        repositoryConfiguration = new PackageMaterialProperties();
        repositoryConfiguration.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("file://invalid_random_2q342340"));
        CheckConnectionResultMessage result = poller.checkConnectionToPackage(packageConfiguration, repositoryConfiguration);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().get(0), is("Could not access file - file://invalid_random_2q342340/repodata/repomd.xml. Invalid file path."));
    }

    @Test
    public void shouldValidatePackageDataWhileTestingConnection() {
        CheckConnectionResultMessage result = poller.checkConnectionToPackage(new PackageMaterialProperties(), repositoryConfiguration);
        assertThat(result.success(), is(false));
        assertThat(result.getMessages().get(0), is("Package spec not specified"));
    }

    @After
    public void tearDown() throws Exception {
        RepoqueryCacheCleaner.performCleanup();
    }


    private long fromEpochTime(long timeInSeconds) {
        return timeInSeconds * 1000;
    }

}