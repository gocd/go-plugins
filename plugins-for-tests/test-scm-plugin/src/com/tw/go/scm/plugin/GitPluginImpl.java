package com.tw.go.scm.plugin;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.tw.go.scm.plugin.jgit.GitHelper;
import com.tw.go.scm.plugin.jgit.JGitHelper;
import com.tw.go.scm.plugin.model.GitConfig;
import com.tw.go.scm.plugin.model.ModifiedFile;
import com.tw.go.scm.plugin.model.Revision;
import com.tw.go.scm.plugin.util.ListUtil;
import com.tw.go.scm.plugin.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Extension
public class GitPluginImpl implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(GitPluginImpl.class);

    public static final String EXTENSION_NAME = "scm";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final String REQUEST_SCM_CONFIGURATION = "scm-configuration";
    public static final String REQUEST_SCM_VIEW = "scm-view";
    public static final String REQUEST_VALIDATE_SCM_CONFIGURATION = "validate-scm-configuration";
    public static final String REQUEST_CHECK_SCM_CONNECTION = "check-scm-connection";
    public static final String REQUEST_LATEST_REVISION = "latest-revision";
    public static final String REQUEST_LATEST_REVISIONS_SINCE = "latest-revisions-since";
    public static final String REQUEST_CHECKOUT = "checkout";

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        // ignore
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        if (goPluginApiRequest.requestName().equals(REQUEST_SCM_CONFIGURATION)) {
            return handleSCMConfiguration();
        } else if (goPluginApiRequest.requestName().equals(REQUEST_SCM_VIEW)) {
            try {
                return handleSCMView();
            } catch (IOException e) {
                String message = "Failed to find template: " + e.getMessage();
                return renderJSON(500, message);
            }
        } else if (goPluginApiRequest.requestName().equals(REQUEST_VALIDATE_SCM_CONFIGURATION)) {
            return handleSCMValidation(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(REQUEST_CHECK_SCM_CONNECTION)) {
            return handleSCMCheckConnection(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(REQUEST_LATEST_REVISION)) {
            return handleGetLatestRevision(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(REQUEST_LATEST_REVISIONS_SINCE)) {
            return handleLatestRevisionSince(goPluginApiRequest);
        } else if (goPluginApiRequest.requestName().equals(REQUEST_CHECKOUT)) {
            return handleCheckout(goPluginApiRequest);
        }
        return null;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
    }

    private GoPluginApiResponse handleSCMConfiguration() {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("url", createField("URL", null, true, true, false, "0"));
        response.put("username", createField("Username", null, false, false, false, "1"));
        response.put("password", createField("Password", null, false, false, true, "2"));
        response.put("branch", createField("Branch", null, true, false, false, "3"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleSCMView() throws IOException {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("displayValue", "JGit");
        response.put("template", IOUtils.toString(getClass().getResourceAsStream("/scm.template.html"), "UTF-8"));
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleSCMValidation(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) parseJSON(goPluginApiRequest.requestBody());
        Map<String, String> configuration = keyValuePairs(responseMap, "scm-configuration");
        final GitConfig gitConfig = getGitConfig(configuration);

        List<Map<String, Object>> response = new ArrayList<Map<String, Object>>();

        validate(response, new FieldValidator() {
            @Override
            public void validate(Map<String, Object> fieldValidation) {
                validateUrl(gitConfig, fieldValidation);
            }
        });

        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleSCMCheckConnection(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) parseJSON(goPluginApiRequest.requestBody());
        Map<String, String> configuration = keyValuePairs(responseMap, "scm-configuration");
        GitConfig gitConfig = getGitConfig(configuration);

        Map<String, Object> response = new HashMap<String, Object>();
        ArrayList<String> messages = new ArrayList<String>();

        checkConnection(gitConfig, response, messages);

        if (response.get("status") == null) {
            response.put("status", "success");
            messages.add("Could connect to URL successfully");
        }
        response.put("messages", messages);
        return renderJSON(SUCCESS_RESPONSE_CODE, response);
    }

    private GoPluginApiResponse handleGetLatestRevision(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) parseJSON(goPluginApiRequest.requestBody());
        Map<String, String> configuration = keyValuePairs(responseMap, "scm-configuration");
        GitConfig gitConfig = getGitConfig(configuration);
        String flyweightFolder = (String) responseMap.get("flyweight-folder");

        LOGGER.warn("flyweight: " + flyweightFolder);

        Map<String, Object> fieldMap = new HashMap<String, Object>();
        validateUrl(gitConfig, fieldMap);
        if (!fieldMap.isEmpty()) {
            LOGGER.warn("invalid url");
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, null);
        }

        try {
            GitHelper git = getJGitHelper(gitConfig, flyweightFolder);
            git.cloneOrFetch();
            Revision revision = git.getLatestRevision();

            if (revision == null) {
                return renderJSON(SUCCESS_RESPONSE_CODE, null);
            } else {
                Map<String, Object> response = new HashMap<String, Object>();
                Map<String, Object> revisionMap = getRevisionMap(revision);
                response.put("revision", revisionMap);
                return renderJSON(SUCCESS_RESPONSE_CODE, response);
            }
        } catch (Throwable t) {
            LOGGER.warn("get latest revision: ", t);
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, null);
        }
    }

    private GoPluginApiResponse handleLatestRevisionSince(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) parseJSON(goPluginApiRequest.requestBody());
        Map<String, String> configuration = keyValuePairs(responseMap, "scm-configuration");
        GitConfig gitConfig = getGitConfig(configuration);
        String flyweightFolder = (String) responseMap.get("flyweight-folder");
        Map<String, Object> previousRevisionMap = (Map<String, Object>) responseMap.get("previous-revision");
        String previousRevision = (String) previousRevisionMap.get("revision");

        LOGGER.warn("flyweight: " + flyweightFolder + ". previous commit: " + previousRevision);

        Map<String, Object> fieldMap = new HashMap<String, Object>();
        validateUrl(gitConfig, fieldMap);
        if (!fieldMap.isEmpty()) {
            LOGGER.warn("invalid url");
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, null);
        }

        try {
            GitHelper git = getJGitHelper(gitConfig, flyweightFolder);
            git.cloneOrFetch();
            List<Revision> newerRevisions = git.getRevisionsSince(previousRevision);

            if (ListUtil.isEmpty(newerRevisions)) {
                return renderJSON(SUCCESS_RESPONSE_CODE, null);
            } else {
                LOGGER.warn("new commits: " + newerRevisions.size());

                Map<String, Object> response = new HashMap<String, Object>();
                List<Map> revisions = new ArrayList<Map>();
                for (Revision revisionObj : newerRevisions) {
                    Map<String, Object> revisionMap = getRevisionMap(revisionObj);
                    revisions.add(revisionMap);
                }
                response.put("revisions", revisions);
                return renderJSON(SUCCESS_RESPONSE_CODE, response);
            }
        } catch (Throwable t) {
            LOGGER.warn("get latest revisions since: ", t);
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, null);
        }
    }

    private GoPluginApiResponse handleCheckout(GoPluginApiRequest goPluginApiRequest) {
        Map<String, Object> responseMap = (Map<String, Object>) parseJSON(goPluginApiRequest.requestBody());
        Map<String, String> configuration = keyValuePairs(responseMap, "scm-configuration");
        GitConfig gitConfig = getGitConfig(configuration);
        String destinationFolder = (String) responseMap.get("destination-folder");
        Map<String, Object> revisionMap = (Map<String, Object>) responseMap.get("revision");
        String revision = (String) revisionMap.get("revision");

        LOGGER.warn("destination: " + destinationFolder + ". commit: " + revision);

        try {
            GitHelper git = getJGitHelper(gitConfig, destinationFolder);
            git.cloneOrFetch();
            git.resetHard(revision);

            Map<String, Object> response = new HashMap<String, Object>();
            ArrayList<String> messages = new ArrayList<String>();
            response.put("status", "success");
            messages.add("Checked out to revision " + revision);
            response.put("messages", messages);

            return renderJSON(SUCCESS_RESPONSE_CODE, response);
        } catch (Throwable t) {
            LOGGER.warn("checkout: ", t);
            return renderJSON(INTERNAL_ERROR_RESPONSE_CODE, null);
        }
    }

    private GitConfig getGitConfig(Map<String, String> configuration) {
        return new GitConfig(configuration.get("url"), configuration.get("username"), configuration.get("password"), configuration.get("branch"));
    }

    private GitHelper getJGitHelper(GitConfig gitConfig, String destinationFolder) {
        File workingDirectory = destinationFolder == null ? null : new File(destinationFolder);
        return new JGitHelper(gitConfig, workingDirectory);
    }

    private Object parseJSON(String json) {
        return new GsonBuilder().create().fromJson(json, Object.class);
    }

    private void validate(List<Map<String, Object>> response, FieldValidator fieldValidator) {
        Map<String, Object> fieldValidation = new HashMap<String, Object>();
        fieldValidator.validate(fieldValidation);
        if (!fieldValidation.isEmpty()) {
            response.add(fieldValidation);
        }
    }

    private Map<String, Object> getRevisionMap(Revision revision) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("revision", revision.getRevision());
        response.put("timestamp", new SimpleDateFormat(DATE_PATTERN).format(revision.getTimestamp()));
        response.put("user", revision.getUser());
        response.put("revisionComment", revision.getComment());
        List<Map> modifiedFilesMapList = new ArrayList<Map>();
        if (!ListUtil.isEmpty(revision.getModifiedFiles())) {
            for (ModifiedFile modifiedFile : revision.getModifiedFiles()) {
                Map<String, String> modifiedFileMap = new HashMap<String, String>();
                modifiedFileMap.put("fileName", modifiedFile.getFileName());
                modifiedFileMap.put("action", modifiedFile.getAction());
                modifiedFilesMapList.add(modifiedFileMap);
            }
        }
        response.put("modifiedFiles", modifiedFilesMapList);
        return response;
    }

    private Map<String, String> keyValuePairs(Map<String, Object> map, String mainKey) {
        Map<String, String> keyValuePairs = new HashMap<String, String>();
        Map<String, Object> fieldsMap = (Map<String, Object>) map.get(mainKey);
        for (String field : fieldsMap.keySet()) {
            Map<String, Object> fieldProperties = (Map<String, Object>) fieldsMap.get(field);
            String value = (String) fieldProperties.get("value");
            keyValuePairs.put(field, value);
        }
        return keyValuePairs;
    }

    private Map<String, Object> createField(String displayName, String defaultValue, boolean isPartOfIdentity, boolean isRequired, boolean isSecure, String displayOrder) {
        Map<String, Object> fieldProperties = new HashMap<String, Object>();
        fieldProperties.put("display-name", displayName);
        fieldProperties.put("default-value", defaultValue);
        fieldProperties.put("part-of-identity", isPartOfIdentity);
        fieldProperties.put("required", isRequired);
        fieldProperties.put("secure", isSecure);
        fieldProperties.put("display-order", displayOrder);
        return fieldProperties;
    }

    public void validateUrl(GitConfig gitConfig, Map<String, Object> fieldMap) {
        if (StringUtil.isEmpty(gitConfig.getUrl())) {
            fieldMap.put("key", "url");
            fieldMap.put("message", "URL is a required field");
        } else {
            if (gitConfig.getUrl().startsWith("/")) {
                if (!new File(gitConfig.getUrl()).exists()) {
                    fieldMap.put("key", "url");
                    fieldMap.put("message", "Invalid URL. Directory does not exist");
                }
            } else {
                if (!isValidURL(gitConfig.getUrl())) {
                    fieldMap.put("key", "url");
                    fieldMap.put("message", "Invalid URL format");
                }
            }
        }
    }

    public void checkConnection(GitConfig gitConfig, Map<String, Object> response, ArrayList<String> messages) {
        try {
            if (StringUtil.isEmpty(gitConfig.getUrl())) {
                response.put("status", "failure");
                messages.add("URL is empty");
            } else if (gitConfig.getUrl().startsWith("/")) {
                if (!new File(gitConfig.getUrl()).exists()) {
                    response.put("status", "failure");
                    messages.add("Could not find Git repository");
                } else {
                    GitHelper gitHelper = getJGitHelper(gitConfig, null);
                    gitHelper.checkConnection();
                }
            } else {
                if (!isValidURL(gitConfig.getUrl())) {
                    response.put("status", "failure");
                    messages.add("Invalid URL format");
                } else {
                    try {
                        GitHelper gitHelper = getJGitHelper(gitConfig, null);
                        gitHelper.checkConnection();
                    } catch (Exception e) {
                        response.put("status", "failure");
                        messages.add("ls-remote failed");
                    }
                }
            }
        } catch (Exception e) {
            response.put("status", "failure");
            if (e.getMessage() != null) {
                messages.add(e.getMessage());
            } else {
                messages.add(e.getClass().getCanonicalName());
            }
        }
    }

    private boolean isValidURL(String url) {
        return new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS).isValid(url);
    }

    private GoPluginApiResponse renderJSON(final int responseCode, Object response) {
        final String json = response == null ? null : new GsonBuilder().create().toJson(response);
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return null;
            }

            @Override
            public String responseBody() {
                return json;
            }
        };
    }
}
