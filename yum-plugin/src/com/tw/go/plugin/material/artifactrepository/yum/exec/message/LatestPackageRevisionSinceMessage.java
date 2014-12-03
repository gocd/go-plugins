package com.tw.go.plugin.material.artifactrepository.yum.exec.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class LatestPackageRevisionSinceMessage {

    @Expose
    @SerializedName("repository-configuration")
    private Map<String, PackageMaterialProperty> repositoryConfiguration;

    @Expose
    @SerializedName("package-configuration")
    private Map<String, PackageMaterialProperty> packageConfiguration;

    @Expose
    @SerializedName("previous-revision")
    private PackageRevisionMessage previousRevision;

    public PackageMaterialProperties getRepositoryConfiguration() {
        return new PackageMaterialProperties(repositoryConfiguration);
    }

    public PackageMaterialProperties getPackageConfiguration() {
        return new PackageMaterialProperties(packageConfiguration);
    }

    public PackageRevisionMessage getPreviousRevision() {
        return previousRevision;
    }
}
