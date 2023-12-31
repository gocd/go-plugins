package com.tw.go.scm.plugin.model;

public class ModifiedFile {
    private final String fileName;
    private final String action;

    public ModifiedFile(String fileName, String action) {
        this.fileName = fileName;
        this.action = action;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "ModifiedFile{" +
                "fileName='" + fileName + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
