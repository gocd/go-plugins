package com.tw.go.plugin.material.artifactrepository.yum.exec.message;

import com.google.gson.annotations.Expose;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PackageRevisionMessage {

    @Expose
    private String revision;

    @Expose
    private Date timestamp;

    @Expose
    private String user;

    @Expose
    private String revisionComment;

    @Expose
    private String trackbackUrl;

    @Expose
    private Map<String, String> data = new HashMap<String, String>();

    public PackageRevisionMessage() {
    }

    public PackageRevisionMessage(String revision, Date timestamp, String user, String revisionComment, String trackbackUrl) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.user = user;
        this.revisionComment = revisionComment;
        this.trackbackUrl = trackbackUrl;
    }

    public String getRevision() {
        return revision;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getRevisionComment() {
        return revisionComment;
    }

    public String getTrackbackUrl() {
        return trackbackUrl;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void addData(String key, String value) {
        data.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageRevisionMessage that = (PackageRevisionMessage) o;

        if (revision != null ? !revision.equals(that.revision) : that.revision != null) {
            return false;
        }
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = revision != null ? revision.hashCode() : 0;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    public String getDataFor(String key) {
        return data.get(key);
    }
}
