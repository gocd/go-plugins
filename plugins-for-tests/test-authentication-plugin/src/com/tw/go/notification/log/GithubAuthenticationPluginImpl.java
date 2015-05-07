package com.tw.go.notification.log;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.io.FileUtils;
import org.brickred.socialauth.*;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;

@Extension
public class GithubAuthenticationPluginImpl implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(GithubAuthenticationPluginImpl.class);

    public static final String GITHUB = "github";

    public static final String EXTENSION_NAME = "authentication";
    private static final List<String> goSupportedVersions = asList("1.0");

    public static final int SUCCESS_RESPONSE_CODE = 200;
    public static final int REDIRECT_RESPONSE_CODE = 302;
    public static final int INTERNAL_ERROR_RESPONSE_CODE = 500;

    private GoApplicationAccessor goApplicationAccessor;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        this.goApplicationAccessor = goApplicationAccessor;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) {
        String requestName = goPluginApiRequest.requestName();
        if (requestName.equals("plugin-configuration")) {
            Map<String, Object> configuration = new HashMap<String, Object>();
            configuration.put("display-name", "Github");
            configuration.put("supports-user-search", false);
            return renderResponse(SUCCESS_RESPONSE_CODE, null, new Gson().toJson(configuration));
        }
        if (requestName.equals("index")) {
            return handleSetupLoginWebRequest(goPluginApiRequest);
        }
        if (requestName.equals("authenticate")) {
            return handleAuthenticateWebRequest(goPluginApiRequest);
        }
        return null;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
    }

    private GoPluginApiResponse handleSetupLoginWebRequest(GoPluginApiRequest goPluginApiRequest) {
        try {
            Properties oauthConsumerProperties = new Properties();
            oauthConsumerProperties.load(getClass().getResourceAsStream("/oauth_consumer.properties"));
            SocialAuthConfig socialAuthConfiguration = SocialAuthConfig.getDefault();
            socialAuthConfiguration.load(oauthConsumerProperties);
            SocialAuthManager manager = new SocialAuthManager();
            manager.setSocialAuthConfig(socialAuthConfiguration);
            String redirectURL = manager.getAuthenticationUrl(GITHUB, getURL(), Permission.ALL);

            store(manager);

            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("Location", redirectURL);
            return renderResponse(REDIRECT_RESPONSE_CODE, responseHeaders, null);
        } catch (Exception e) {
            LOGGER.error("Error occurred while Github OAuth setup.", e);
            return renderResponse(INTERNAL_ERROR_RESPONSE_CODE, null, null);
        }
    }

    private GoPluginApiResponse handleAuthenticateWebRequest(final GoPluginApiRequest goPluginApiRequest) {
        try {
            SocialAuthManager manager = read();
            if (manager == null) {
                throw new RuntimeException("socialauth manager not set");
            }

            AuthProvider provider = manager.connect(goPluginApiRequest.requestParameters());
            Profile profile = provider.getUserProfile();

            String userId = profile.getValidatedId();
            String displayName = profile.getDisplayName();
            String fullName = profile.getFullName();
            String email = profile.getEmail();
            email = email == null ? email : email.toLowerCase().trim();
            LOGGER.error(userId + " - " + displayName + " - " + fullName + " - " + email);

            // TODO: Tell Go Server user is authenticated & provide details (username, email etc.)

            delete();

            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("Location", getServerBaseURL());
            return renderResponse(REDIRECT_RESPONSE_CODE, responseHeaders, null);
        } catch (Exception e) {
            LOGGER.error("Error occurred while Github OAuth authenticate.", e);
            return renderResponse(INTERNAL_ERROR_RESPONSE_CODE, null, null);
        }
    }

    // TODO: this needs to be stored in session
    private void store(SocialAuthManager socialAuthManager) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("/tmp/social-auth");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(socialAuthManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: this needs to be read from session
    private SocialAuthManager read() {
        try {
            FileInputStream fileInputStream = new FileInputStream("/tmp/social-auth");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            return (SocialAuthManager) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: this needs to be deleted from session
    private void delete() {
        FileUtils.deleteQuietly(new File("/tmp/social-auth"));
    }

    private String getURL() {
        return String.format("%s/go/plugins/interact/github.authenticator/authenticate", getServerBaseURL());
    }

    // TODO: this needs to be dynamic (system property)
    private String getServerBaseURL() {
        return "http://" + "localhost:8153";
    }

    private GoPluginApiResponse renderResponse(final int responseCode, final Map<String, String> responseHeaders, final String responseBody) {
        return new GoPluginApiResponse() {
            @Override
            public int responseCode() {
                return responseCode;
            }

            @Override
            public Map<String, String> responseHeaders() {
                return responseHeaders;
            }

            @Override
            public String responseBody() {
                return responseBody;
            }
        };
    }
}
