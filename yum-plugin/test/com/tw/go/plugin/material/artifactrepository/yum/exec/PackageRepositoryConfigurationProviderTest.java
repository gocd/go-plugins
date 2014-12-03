package com.tw.go.plugin.material.artifactrepository.yum.exec;

import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageMaterialProperties;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.PackageMaterialProperty;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.ValidationError;
import com.tw.go.plugin.material.artifactrepository.yum.exec.message.ValidationResultMessage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class PackageRepositoryConfigurationProviderTest {

    private PackageRepositoryConfigurationProvider configurationProvider;

    @Before
    public void setUp() throws Exception {
        configurationProvider = new PackageRepositoryConfigurationProvider();
    }

    @Test
    public void shouldGetRepositoryConfiguration() {

        PackageMaterialProperties configuration = configurationProvider.repositoryConfiguration();

        assertThat(configuration.getProperty(Constants.REPO_URL), notNullValue());
        assertThat(configuration.getProperty(Constants.REPO_URL).partOfIdentity(), nullValue());
        assertThat(configuration.getProperty(Constants.REPO_URL).required(), nullValue());
        assertThat(configuration.getProperty(Constants.REPO_URL).secure(), nullValue());
        assertThat(configuration.getProperty(Constants.REPO_URL).displayName(), is("Repository URL"));
        assertThat(configuration.getProperty(Constants.REPO_URL).displayOrder(), is("0"));

        assertThat(configuration.getProperty(Constants.USERNAME), notNullValue());
        assertThat(configuration.getProperty(Constants.USERNAME).partOfIdentity(), is(false));
        assertThat(configuration.getProperty(Constants.USERNAME).required(), is(false));
        assertThat(configuration.getProperty(Constants.USERNAME).secure(), nullValue());
        assertThat(configuration.getProperty(Constants.USERNAME).displayName(), is("User"));
        assertThat(configuration.getProperty(Constants.USERNAME).displayOrder(), is("1"));

        assertThat(configuration.getProperty(Constants.PASSWORD), notNullValue());
        assertThat(configuration.getProperty(Constants.PASSWORD).partOfIdentity(), is(false));
        assertThat(configuration.getProperty(Constants.PASSWORD).required(), is(false));
        assertThat(configuration.getProperty(Constants.PASSWORD).secure(), is(true));
        assertThat(configuration.getProperty(Constants.PASSWORD).displayName(), is("Password"));
        assertThat(configuration.getProperty(Constants.PASSWORD).displayOrder(), is("2"));
    }

    @Test
    public void shouldGetPackageConfiguration() {
        PackageMaterialProperties configuration = configurationProvider.packageConfiguration();
        assertThat(configuration.getProperty(Constants.PACKAGE_SPEC), notNullValue());
        assertThat(configuration.getProperty(Constants.PACKAGE_SPEC).displayName(), is("Package Spec"));
        assertThat(configuration.getProperty(Constants.PACKAGE_SPEC).displayOrder(), is("0"));
    }

    @Test
    public void shouldCheckIfRepositoryConfigurationValid() {
        assertConfigurationErrors(configurationProvider.validateRepositoryConfiguration(new PackageMaterialProperties()), asList(new ValidationError(Constants.REPO_URL, "Repository url not specified")), false);
        assertConfigurationErrors(configurationProvider.validateRepositoryConfiguration(configurations(Constants.REPO_URL, null)), asList(new ValidationError(Constants.REPO_URL, "Repository url is empty")), false);
        assertConfigurationErrors(configurationProvider.validateRepositoryConfiguration(configurations(Constants.REPO_URL, "")), asList(new ValidationError(Constants.REPO_URL, "Repository url is empty")), false);
        assertConfigurationErrors(configurationProvider.validateRepositoryConfiguration(configurations(Constants.REPO_URL, "incorrectUrl")), asList(new ValidationError(Constants.REPO_URL, "Invalid URL : incorrectUrl")), false);
        assertConfigurationErrors(configurationProvider.validateRepositoryConfiguration(configurations(Constants.REPO_URL, "http://correct.com/url")), new ArrayList<ValidationError>(), true);
        assertConfigurationErrors(configurationProvider.validateRepositoryConfiguration(configurations(Constants.REPO_URL, "http://correct.com/url")), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldCheckForInvalidKeyInRepositoryConfiguration() {
        PackageMaterialProperties configurationProvidedByUser = new PackageMaterialProperties();
        configurationProvidedByUser.addPackageMaterialProperty(Constants.REPO_URL, new PackageMaterialProperty().withValue("http://correct.com/url"));
        configurationProvidedByUser.addPackageMaterialProperty("invalid-keys", new PackageMaterialProperty().withValue("some value"));
        ValidationResultMessage validationResultMessage = configurationProvider.validateRepositoryConfiguration(configurationProvidedByUser);
        assertConfigurationErrors(validationResultMessage, asList(new ValidationError("", "Unsupported key(s) found : invalid-keys. Allowed key(s) are : REPO_URL, USERNAME, PASSWORD")), false);
    }


    @Test
    public void shouldCheckIfPackageConfigurationValid() {
        assertConfigurationErrors(configurationProvider.validatePackageConfiguration(new PackageMaterialProperties()), asList(new ValidationError(Constants.PACKAGE_SPEC, "Package spec not specified")), false);
        assertConfigurationErrors(configurationProvider.validatePackageConfiguration(configurations(Constants.PACKAGE_SPEC, null)), asList(new ValidationError(Constants.PACKAGE_SPEC, "Package spec is null")), false);
        assertConfigurationErrors(configurationProvider.validatePackageConfiguration(configurations(Constants.PACKAGE_SPEC, "")), asList(new ValidationError(Constants.PACKAGE_SPEC, "Package spec is empty")), false);
        assertConfigurationErrors(configurationProvider.validatePackageConfiguration(configurations(Constants.PACKAGE_SPEC, "go-age?nt-*")), new ArrayList<ValidationError>(), true);
        assertConfigurationErrors(configurationProvider.validatePackageConfiguration(configurations(Constants.PACKAGE_SPEC, "go-agent")), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldCheckForInvalidKeyInPackageConfiguration() {
        PackageMaterialProperties configurationProvidedByUser = new PackageMaterialProperties();
        configurationProvidedByUser.addPackageMaterialProperty(Constants.PACKAGE_SPEC, new PackageMaterialProperty().withValue("go-agent"));
        configurationProvidedByUser.addPackageMaterialProperty("invalid-keys", new PackageMaterialProperty().withValue("some value"));
        ValidationResultMessage validationResultMessage = configurationProvider.validatePackageConfiguration(configurationProvidedByUser);
        assertConfigurationErrors(validationResultMessage, asList(new ValidationError("", "Unsupported key(s) found : invalid-keys. Allowed key(s) are : PACKAGE_SPEC")), false);
    }

    private void assertConfigurationErrors(ValidationResultMessage validationResult, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        assertThat(validationResult.success(), is(expectedValidationResult));
        assertThat(validationResult.getValidationErrors().size(), is(expectedErrors.size()));
        assertThat(validationResult.getValidationErrors().containsAll(expectedErrors), is(true));
    }

    private PackageMaterialProperties configurations(String key, String value) {
        PackageMaterialProperties configurations = new PackageMaterialProperties();
        configurations.addPackageMaterialProperty(key, new PackageMaterialProperty().withValue(value));
        return configurations;
    }
}
