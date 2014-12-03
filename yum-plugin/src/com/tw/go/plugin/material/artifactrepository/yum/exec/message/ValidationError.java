package com.tw.go.plugin.material.artifactrepository.yum.exec.message;

import com.google.gson.annotations.Expose;

public class ValidationError {

    @Expose
    private String key;

    @Expose
    private String message;

    public ValidationError(String key, String message) {
        this.key = key;
        this.message = message;
    }

    public static ValidationError create(String message) {
        return new ValidationError("", message);
    }

    public static ValidationError create(String key, String message) {
        return new ValidationError(key, message);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationError that = (ValidationError) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
