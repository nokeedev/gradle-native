/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nokee.ide.xcode.internal.xcodeproj;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import dev.nokee.ide.xcode.XcodeIdeProductType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Information for building a specific artifact (a library, binary, or test).
 */
public abstract class PBXTarget extends PBXProjectItem {
    private final String name;
    private final XcodeIdeProductType productType;
    private final List<PBXBuildPhase> buildPhases;
    private final XCConfigurationList buildConfigurationList;
    @Nullable
    private String productName;
    @Nullable
    private PBXFileReference productReference;
    public PBXTarget(String name, XcodeIdeProductType productType) {
        this.name = Preconditions.checkNotNull(name);
        this.productType = Preconditions.checkNotNull(productType);
        this.buildPhases = Lists.newArrayList();
        this.buildConfigurationList = new XCConfigurationList();
    }

    public String getName() {
        return name;
    }

    public XcodeIdeProductType getProductType() {
        return productType;
    }

    public List<PBXBuildPhase> getBuildPhases() {
        return buildPhases;
    }

    public XCConfigurationList getBuildConfigurationList() {
        return buildConfigurationList;
    }

    @Nullable
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Nullable
    public PBXFileReference getProductReference() {
        return productReference;
    }

    public void setProductReference(PBXFileReference v) {
        productReference = v;
    }

    @Override
    public String isa() {
        return "PBXTarget";
    }

    @Override
    public int stableHash() {
        return name.hashCode();
    }

    @Override
    public void serializeInto(XcodeprojSerializer s) {
        super.serializeInto(s);

        s.addField("name", name);
        if (productType != null) {
            s.addField("productType", productType.toString());
        }
        if (productName != null) {
            s.addField("productName", productName);
        }
        if (productReference != null) {
            s.addField("productReference", productReference);
        }
        s.addField("buildPhases", buildPhases);
        if (buildConfigurationList != null) {
            s.addField("buildConfigurationList", buildConfigurationList);
        }
    }
}
