package com.tw.go.notification.log;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.brickred.socialauth.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import static java.util.Arrays.asList;

@Extension
public class GithubAuthenticationPluginImpl implements GoPlugin {
    private static Logger LOGGER = Logger.getLoggerFor(GithubAuthenticationPluginImpl.class);

    public static final String PLUGIN_CONFIGURATION = "plugin-configuration";
    public static final String AUTHENTICATE_USER = "authenticate-user";
    public static final String GET_USER_DETAILS = "get-user-details";
    public static final String INDEX_WEB_REQUEST = "index";
    public static final String AUTHENTICATE_WEB_REQUEST = "authenticate";
    public static final String TEST_WEB_REQUEST = "test";

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
        if (requestName.equals(PLUGIN_CONFIGURATION)) {
            Map<String, Object> configuration = new HashMap<String, Object>();
            configuration.put("display-name", "Github");
            configuration.put("supports-password-based-authentication", true);
            configuration.put("supports-user-search", false);
            return renderResponse(SUCCESS_RESPONSE_CODE, null, new Gson().toJson(configuration));
        }
        if (requestName.equals(AUTHENTICATE_USER)) {
            Map<String, Object> requestBodyMap = new Gson().fromJson(goPluginApiRequest.requestBody(), Map.class);
            String username = (String) requestBodyMap.get("username");
            String password = (String) requestBodyMap.get("password");
            Map<String, Object> responseMap = new HashMap<String, Object>();
            List<String> messages = new ArrayList<String>();
            if (username.equals("test") && password.equals("test")) {
                responseMap.put("status", "success");
                messages.add("successful authentication.");
            } else {
                responseMap.put("status", "failure");
                messages.add("authentication failed.");
            }
            responseMap.put("messages", messages);
            return renderResponse(SUCCESS_RESPONSE_CODE, null, new Gson().toJson(responseMap));
        }
        if (requestName.equals(GET_USER_DETAILS)) {
            return renderResponse(SUCCESS_RESPONSE_CODE, null, getUserJSON("test", "test", "first", "last", ""));
        }
        if (requestName.equals(INDEX_WEB_REQUEST)) {
            return handleSetupLoginWebRequest(goPluginApiRequest);
        }
        if (requestName.equals(AUTHENTICATE_WEB_REQUEST)) {
            return handleAuthenticateWebRequest(goPluginApiRequest);
        }
        if (requestName.equals(TEST_WEB_REQUEST)) {
            return renderResponse(SUCCESS_RESPONSE_CODE, null, getFileContents("/views/test.html"));
        }
        return null;
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return getGoPluginIdentifier();
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
            String firstName = isEmpty(fullName) ? displayName : fullName.split(" ")[0];
            String lastName = isEmpty(fullName) || fullName.split(" ").length == 1 ? "" : fullName.split(" ")[1];
            String emailId = profile.getEmail();
            emailId = emailId == null ? emailId : emailId.toLowerCase().trim();
            LOGGER.error(userId + " - " + displayName + " - " + firstName + " - " + lastName + " - " + emailId);

            final String userJSON = getUserJSON(userId, displayName, firstName, lastName, emailId);
            GoApiRequest authenticateUserRequest = createGoApiRequest("authenticate-user", userJSON);
            GoApiResponse authenticateUserResponse = goApplicationAccessor.submit(authenticateUserRequest);
            // handle error

            delete();

            Map<String, String> responseHeaders = new HashMap<String, String>();
            responseHeaders.put("Location", getServerBaseURL());
            return renderResponse(REDIRECT_RESPONSE_CODE, responseHeaders, null);
        } catch (Exception e) {
            LOGGER.error("Error occurred while Github OAuth authenticate.", e);
            return renderResponse(INTERNAL_ERROR_RESPONSE_CODE, null, null);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void store(SocialAuthManager socialAuthManager) {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("plugin-id", "github.authenticator");
        Map<String, Object> sessionData = new HashMap<String, Object>();
        String socialAuthManagerStr = serializeObject(socialAuthManager);
        sessionData.put("social-auth-manager", socialAuthManagerStr);
        requestMap.put("session-data", sessionData);
        GoApiRequest goApiRequest = createGoApiRequest("store-in-session", new Gson().toJson(requestMap));
        GoApiResponse response = goApplicationAccessor.submit(goApiRequest);
        // handle error
    }

    private String serializeObject(SocialAuthManager socialAuthManager) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(socialAuthManager);
            objectOutputStream.flush();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return new String(Base64.encodeBase64(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SocialAuthManager read() {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("plugin-id", "github.authenticator");
        GoApiRequest goApiRequest = createGoApiRequest("get-from-session", new Gson().toJson(requestMap));
        GoApiResponse response = goApplicationAccessor.submit(goApiRequest);
        // handle error
        String responseBody = response.responseBody();
        Map<String, Object> sessionData = new Gson().fromJson(responseBody, Map.class);
        String socialAuthManagerStr = (String) sessionData.get("social-auth-manager");
        return deserializeObject(socialAuthManagerStr);
    }

    private SocialAuthManager deserializeObject(String socialAuthManagerStr) {
        try {
            byte bytes[] = Base64.decodeBase64(socialAuthManagerStr.getBytes());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (SocialAuthManager) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void delete() {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("plugin-id", "github.authenticator");
        GoApiRequest goApiRequest = createGoApiRequest("remove-from-session", new Gson().toJson(requestMap));
        GoApiResponse response = goApplicationAccessor.submit(goApiRequest);
        // handle error
    }

    private String getURL() {
        return String.format("%s/go/plugins/interact/github.authenticator/authenticate", getServerBaseURL());
    }

    // TODO: this needs to be dynamic (system property)
    private String getServerBaseURL() {
        return "http://" + "localhost:8153";
    }

    private String getFileContents(String filePath) {
        try {
            return IOUtils.toString(getClass().getResource(filePath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GoApiRequest createGoApiRequest(final String api, final String responseBody) {
        return new GoApiRequest() {
            @Override
            public String api() {
                return api;
            }

            @Override
            public String apiVersion() {
                return null;
            }

            @Override
            public GoPluginIdentifier pluginIdentifier() {
                return getGoPluginIdentifier();
            }

            @Override
            public Map<String, String> requestParameters() {
                return null;
            }

            @Override
            public Map<String, String> requestHeaders() {
                return null;
            }

            @Override
            public String requestBody() {
                return responseBody;
            }
        };
    }

    private String getUserJSON(String userId, String displayName, String firstName, String lastName, String emailId) {
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("id", userId);
        userMap.put("username", displayName);
        userMap.put("first-name", firstName);
        userMap.put("last-name", lastName);
        userMap.put("email-id", emailId);
        return new Gson().toJson(userMap);
    }

    private GoPluginIdentifier getGoPluginIdentifier() {
        return new GoPluginIdentifier(EXTENSION_NAME, goSupportedVersions);
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
