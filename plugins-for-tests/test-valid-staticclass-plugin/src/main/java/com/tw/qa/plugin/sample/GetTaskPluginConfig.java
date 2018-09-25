package com.tw.qa.plugin.sample;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.HashMap;

public class GetTaskPluginConfig {

    public GoPluginApiResponse execute() {
        HashMap<String, Object> config = new HashMap<>();

        HashMap<String, Object> url = new HashMap<>();
        url.put("display-order", "0");
        url.put("display-name", "Url");
        url.put("required", true);
        config.put("Url", url);

        HashMap<String, Object> secure = new HashMap<>();
        secure.put("default-value", "SecureConnection");
        secure.put("display-order", "1");
        secure.put("display-name", "Secure Connection");
        secure.put("required", false);
        config.put("SecureConnection", secure);

        HashMap<String, Object> requestType = new HashMap<>();
        requestType.put("default-value", "RequestType");
        requestType.put("display-order", "2");
        requestType.put("display-name", "Request Type");
        requestType.put("required", false);
        config.put("RequestType", requestType);

        HashMap<String, Object> additionalOptions = new HashMap<>();
        additionalOptions.put("display-order", "3");
        additionalOptions.put("display-name", "Additional Options");
        additionalOptions.put("required", false);
        config.put("AdditionalOptions", additionalOptions);

        return DefaultGoPluginApiResponse.success(new GsonBuilder().create().toJson(config));
    }
}
