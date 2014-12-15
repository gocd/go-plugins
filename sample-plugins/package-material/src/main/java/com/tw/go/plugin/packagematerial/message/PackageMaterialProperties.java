/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/
package com.tw.go.plugin.packagematerial.message;

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
