package com.tw.go.plugin.material.artifactrepository.yum.exec.message;

import com.google.gson.annotations.Expose;

import java.util.List;

public class CheckConnectionResultMessage {

    public enum STATUS {SUCCESS, FAILURE}

    @Expose
    private STATUS status;

    @Expose
    private List<String> messages;

    public CheckConnectionResultMessage(STATUS status, List<String> messages) {
        this.status = status;
        this.messages = messages;
    }

    public boolean success() {
        return STATUS.SUCCESS.equals(status);
    }

    public List<String> getMessages() {
        return messages;
    }
}
