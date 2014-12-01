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

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import com.tw.go.plugin.common.util.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class YumRepositoryPollerTest {
    private YumRepositoryPoller poller;
    private RepositoryConfiguration repositoryPackageConfigurations;
    private PackageConfiguration packagePackageConfigurations;
    private File sampleRepoDirectory;
    private String repoUrl;

    @Before
    public void setup() throws IOException {
        RepoqueryCacheCleaner.performCleanup();
        repositoryPackageConfigurations = new RepositoryConfiguration();

        sampleRepoDirectory = new File("test/repos/samplerepo");
        repoUrl = "file://" + sampleRepoDirectory.getAbsolutePath();
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, repoUrl));

        packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "go-agent"));

        poller = new YumRepositoryPoller();
    }

    @Test
    public void shouldGetLatestModificationGivenPackageAndRepoConfigurations_getLatestRevision() {
        PackageRevision latestRevision = poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
        assertThat(latestRevision, is(new PackageRevision("go-agent-13.1.1-16714.noarch", new Date(fromEpochTime(1365054258L)), null)));

        assertThat(latestRevision.getDataFor("LOCATION"), is("file://" + sampleRepoDirectory.getAbsolutePath() + "/go-agent-13.1.1-16714.noarch.rpm"));
    }

    @Test
    public void shouldThrowExceptionWhileGettingLatestRevisionIfCheckConnectionFails_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, "file://foo/bar"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Invalid file path."));
        }
    }

    @Test
    public void shouldGetTheRightLocationForAnyPackage_getLatestRevision() {
        PackageConfiguration ppc = new PackageConfiguration();
        ppc.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "php"));
        PackageRevision latestRevision = poller.getLatestRevision(ppc, repositoryPackageConfigurations);

        assertThat(latestRevision, is(new PackageRevision("php-0-0.noarch", new Date(fromEpochTime(1365053593)), null)));
        assertThat(latestRevision.getDataFor("LOCATION"), is("file://" + sampleRepoDirectory.getAbsolutePath() + "/innerFolder/php-0-0.noarch.rpm"));
    }

    @Test
    public void shouldThrowExceptionGivenNonExistingRepo_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, "file://junk-repo"));
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "junk-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage().startsWith("Invalid file path."), is(true));
        }
    }

    @Test
    public void shouldThrowExceptionGivenNonExistingPackageInExistingRepo_getLatestRevision() {
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "junk-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            String expectedMessage = String.format("Error while querying repository with path '%s' and package spec '%s'.", repositoryPackageConfigurations.get(Constants.REPO_URL).getValue(), "junk-artifact");
            assertThat(e.getMessage().startsWith(expectedMessage), is((true)));
        }
    }

    @Test
    public void shouldThrowExceptionGivenEmptyRepo_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        File emptyRepo = new File("test/repos/emptyrepo");
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, "file://" + emptyRepo.getAbsolutePath()));
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "junk-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            String expectedMessage = String.format("Error while querying repository with path '%s' and package spec '%s'.", repositoryPackageConfigurations.get(Constants.REPO_URL).getValue(), "junk-artifact");
            assertThat(e.getMessage().startsWith(expectedMessage), is((true)));
        }
    }

    @Test
    public void shouldPerformRepositoryConfigurationBeforeModificationCheck_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();

        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "junk-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Repository url not specified")));
        }
    }

    @Test
    public void shouldPerformPackageConfigurationBeforeModificationCheck() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        File emptyRepo = new File("test/repos/emptyrepo");
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, "file://" + emptyRepo.getAbsolutePath()));

        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Package spec not specified")));
        }
    }

    @Test
    public void testShouldConcatenateErrorsWhenModificationCheckFails() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Repository url not specified; Package spec not specified")));
        }
    }

    @Test
    public void shouldGetLatestModificationSinceGivenPackageAndRepoConfigurationsAndPreviouslyKnownRevision() {
        PackageRevision latestRevision = poller.latestModificationSince(packagePackageConfigurations, repositoryPackageConfigurations, new PackageRevision("symlinks-1.2-24.2.2.i386", new Date(fromEpochTime(1263710418L)), null));
        assertThat(latestRevision, is(new PackageRevision("go-agent-13.1.1-16714.noarch", new Date(fromEpochTime(1365054258L)), null)));
    }

    @Test
    public void shouldReturnNullGivenPackageAndRepoConfigurationsAndPreviouslyKnownRevision() {
        PackageRevision latestRevision = poller.latestModificationSince(packagePackageConfigurations, repositoryPackageConfigurations, new PackageRevision("go-agent-13.1.1-16714-noarch", new Date(fromEpochTime(1365054258L)), null));
        assertThat(latestRevision, is(nullValue()));
    }

    @Test
    public void shouldReturnNullWhenPreviouslyKnownPackageRevisionIsSameAsCurrent() {
        YumRepositoryPoller spy = spy(poller);
        when(spy.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations)).thenReturn(new PackageRevision("go-agent-13.1.1-16714-noarch", new Date(fromEpochTime(1365054258L)), null));
        PackageRevision latestRevision = poller.latestModificationSince(packagePackageConfigurations, repositoryPackageConfigurations, new PackageRevision("go-agent-13.1.1-16714-noarch", new Date(fromEpochTime(1365054258L)), null));
        assertThat(latestRevision, is(nullValue()));
    }

    @Test
    public void shouldNotThrowUpWhenDataKeyIsInvalid() throws Exception {
        String invalidKey = "!INVALID";
        ReflectionUtil.setStaticField(Constants.class, "PACKAGE_LOCATION", invalidKey);
        try {
            PackageRevision latestRevision = null;
            try {
                latestRevision = poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            } catch (Exception e) {
                fail("should not throw exception");
            }
            assertThat(latestRevision.getDataFor(invalidKey), is(nullValue()));
        } finally {
            ReflectionUtil.setStaticField(Constants.class, "PACKAGE_LOCATION", "LOCATION");
        }
    }

    @Test
    public void shouldThrowExceptionIfCredentialsHaveBeenProvidedAlongWithFileProtocol() {
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.USERNAME, "loser"));
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.PASSWORD, "pwd"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("Should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("File protocol does not support username and/or password."));
        }
    }

    @Test
    public void shouldCheckRepoConnection() throws Exception {
        assertThat(poller.checkConnectionToRepository(repositoryPackageConfigurations).isSuccessful(), is(true));
        assertThat(poller.checkConnectionToRepository(repositoryPackageConfigurations).getMessages().size(), is(1));
        assertThat(poller.checkConnectionToRepository(repositoryPackageConfigurations).getMessages().get(0), is(String.format("Successfully accessed repository metadata at %s", repoUrl + "/repodata/repomd.xml")));
    }

    @Test
    public void shouldReturnErrorsWhenConnectionToRepoFails() throws Exception {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, "file://invalid_path"));

        Result result = poller.checkConnectionToRepository(repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessages().get(0), is("Could not access file - file://invalid_path/repodata/repomd.xml. Invalid file path."));
    }

    @Test
    public void shouldPerformRepoValidationsBeforeCheckConnection() throws Exception {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, "ftp://username:password@invalid_path"));

        Result result = poller.checkConnectionToRepository(repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessages().size(), is(2));
        assertThat(result.getMessages().get(0), is("Invalid URL: Only 'file', 'http' and 'https' protocols are supported."));
        assertThat(result.getMessages().get(1), is("User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
    }

    @Test
    public void shouldCheckConnectionToPackageAndRespondWithLatestPackageFound() {
        Result result = poller.checkConnectionToPackage(packagePackageConfigurations, repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(true));
        assertThat(result.getMessagesForDisplay(), is("Found package 'go-agent-13.1.1-16714.noarch'."));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfPackageIsNotFound() {
        PackageConfiguration packageConfigurations = new PackageConfiguration();
        packageConfigurations.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "go-a"));
        Result result = poller.checkConnectionToPackage(packageConfigurations, repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("Could not find any package that matched 'go-a'."));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfMultiplePackageIsFound() {
        PackageConfiguration packageConfigurations = new PackageConfiguration();
        packageConfigurations.add(new PackageMaterialProperty(Constants.PACKAGE_SPEC, "go*"));
        Result result = poller.checkConnectionToPackage(packageConfigurations, repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay().startsWith("Given Package Spec (go*) resolves to more than one file on the repository: "), is(true));
        assertThat(result.getMessagesForDisplay().contains("go-agent-13.1.1-16714.noarch.rpm"), is(true));
        assertThat(result.getMessagesForDisplay().contains("go-server-13.1.1-16714.noarch.rpm"), is(true));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfRepositoryIsNotReachable() {
        RepositoryConfiguration repositoryConfigurations = new RepositoryConfiguration();
        repositoryConfigurations.add(new PackageMaterialProperty(Constants.REPO_URL, "file://invalid_random_2q342340"));
        Result result = poller.checkConnectionToPackage(packagePackageConfigurations, repositoryConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("Could not access file - file://invalid_random_2q342340/repodata/repomd.xml. Invalid file path."));
    }

    @Test
    public void shouldValidatePackageDataWhileTestingConnection() {
        Result result = poller.checkConnectionToPackage(new PackageConfiguration(), repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("Package spec not specified"));
    }

    private long fromEpochTime(long timeInSeconds) {
        return timeInSeconds * 1000;
    }

    @After
    public void tearDown() throws Exception {
        RepoqueryCacheCleaner.performCleanup();
    }
}
