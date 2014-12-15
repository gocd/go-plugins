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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PackageMaterialProperty {

    @Expose
    private String value;

    @Expose
    private Boolean secure;

    @Expose
    @SerializedName("part-of-identity")
    private Boolean partOfIdentity;

    private Boolean required;

    @Expose
    @SerializedName("display-name")
    private String displayName;

    @Expose
    @SerializedName("display-order")
    private String displayOrder;


    public PackageMaterialProperty withSecure(Boolean secure) {
        this.secure = secure;
        return this;
    }

    public PackageMaterialProperty withPartOfIdentity(Boolean partOfIdentity) {
        this.partOfIdentity = partOfIdentity;
        return this;
    }

    public PackageMaterialProperty withRequired(Boolean required) {
        this.required = required;
        return this;
    }

    public PackageMaterialProperty withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public PackageMaterialProperty withDisplayOrder(String displayOrder) {
        this.displayOrder = displayOrder;
        return this;
    }

    public PackageMaterialProperty withValue(String value) {
        this.value = value;
        return this;
    }

    public String value() {
        return value;
    }

    public Boolean secure() {
        return secure;
    }

    public Boolean partOfIdentity() {
        return partOfIdentity;
    }

    public Boolean required() {
        return required;
    }

    public String displayName() {
        return displayName;
    }

    public String displayOrder() {
        return displayOrder;
    }


}
