package com.tw.go.plugin.material.artifactrepository.yum.exec.message;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class PackageMaterialProperties {

    private Map<String, PackageMaterialProperty> propertyMap = new LinkedHashMap<String, PackageMaterialProperty>();

    public PackageMaterialProperties() {
    }

    public PackageMaterialProperties(Map<String, PackageMaterialProperty> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public void addPackageMaterialProperty(String key, PackageMaterialProperty packageMaterialProperty) {
        propertyMap.put(key, packageMaterialProperty);
    }

    public PackageMaterialProperty getProperty(String key) {
        return propertyMap.get(key);
    }


    public boolean hasKey(String key) {
        return propertyMap.keySet().contains(key);
    }

    public Collection<String> keys() {
        return propertyMap.keySet();
    }

    public Map<String, PackageMaterialProperty> getPropertyMap() {
        return propertyMap;
    }
}
