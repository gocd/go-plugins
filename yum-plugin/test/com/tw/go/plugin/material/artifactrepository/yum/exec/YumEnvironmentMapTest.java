package com.tw.go.plugin.material.artifactrepository.yum.exec;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class YumEnvironmentMapTest {
    @Test
    public void shouldSetHomeEnvToTempWhenDefaultHomeEnvValueIsNotSet() throws Exception {
        Map<String, String> expectedEnvMap = new HashMap<String, String>();
        YumEnvironmentMap yumEnvironmentMap = spy(new YumEnvironmentMap("someId"));
        expectedEnvMap.put(yumEnvironmentMap.HOME, System.getProperty("java.io.tmpdir"));
        doReturn(null).when(yumEnvironmentMap).getSystemEnvVariableFor(yumEnvironmentMap.HOME);

        Map<String, String> actualEnvMap = yumEnvironmentMap.buildYumEnvironmentMap();

        assertThat(actualEnvMap.get(yumEnvironmentMap.HOME), is(expectedEnvMap.get(yumEnvironmentMap.HOME)));
    }

    @Test
    public void shouldSetYumHomeEnvToTheHomeEnvVarValueAndNotDefaultWhenHOMEEnvVarIsSet() throws Exception {
        Map<String, String> expectedEnvMap = new HashMap<String, String>();
        YumEnvironmentMap yumEnvironmentMap = spy(new YumEnvironmentMap("someId"));
        expectedEnvMap.put(yumEnvironmentMap.HOME, System.getProperty("java.io.tmpdir"));
        doReturn("/Users/Ali").when(yumEnvironmentMap).getSystemEnvVariableFor(yumEnvironmentMap.HOME);

        Map<String, String> actualEnvMap = yumEnvironmentMap.buildYumEnvironmentMap();

        assertThat(actualEnvMap.get(yumEnvironmentMap.HOME), is(not(expectedEnvMap.get(yumEnvironmentMap.HOME))));
        assertThat(actualEnvMap.get(yumEnvironmentMap.HOME), is("/Users/Ali"));
    }

    @Test
    public void shouldSetVarTmpAsDefaultTempLocationForYumIfEnvGoYumTmpdirIsNotSpecified() throws Exception {
        Map<String, String> expectedEnvMap = new HashMap<String, String>();
        YumEnvironmentMap yumEnvironmentMap = spy(new YumEnvironmentMap("someId"));
        expectedEnvMap.put(yumEnvironmentMap.TMPDIR, yumEnvironmentMap.defaultTempYumRepoDir);
        doReturn(null).when(yumEnvironmentMap).getSystemEnvVariableFor("go.yum.tmpdir");

        Map<String, String> actualEnvMap = yumEnvironmentMap.buildYumEnvironmentMap();

        assertThat(actualEnvMap.get(yumEnvironmentMap.TMPDIR), containsString(expectedEnvMap.get(yumEnvironmentMap.TMPDIR)));
    }

    @Test
    public void shouldSetTempLocationForYumToTheDirectorySpecifiedBySystemProperty() throws Exception {
        Map<String, String> expectedEnvMap = new HashMap<String, String>();
        YumEnvironmentMap yumEnvironmentMap = spy(new YumEnvironmentMap("someId"));
        expectedEnvMap.put(yumEnvironmentMap.TMPDIR, yumEnvironmentMap.defaultTempYumRepoDir);
        doReturn("some/location").when(yumEnvironmentMap).getSystemPropertyValueFor("go.yum.tmpdir", yumEnvironmentMap.defaultTempYumRepoDir);

        Map<String, String> actualEnvMap = yumEnvironmentMap.buildYumEnvironmentMap();

        assertThat(actualEnvMap.get(yumEnvironmentMap.TMPDIR), containsString("some/location"));
        assertThat(actualEnvMap.get(yumEnvironmentMap.TMPDIR), not(containsString(expectedEnvMap.get(yumEnvironmentMap.TMPDIR))));
    }
}