/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.xcode.project;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Information for building a specific artifact (a library, binary, or test).
 */
public abstract class PBXTarget extends PBXProjectItem {
    private final String name;
    private final String productType;
    private final List<PBXBuildPhase> buildPhases;
    private final XCConfigurationList buildConfigurationList;
    @Nullable private final String productName;
    @Nullable private final PBXFileReference productReference;

	protected PBXTarget(String name, String productType, List<PBXBuildPhase> buildPhases, XCConfigurationList buildConfigurationList, @Nullable String productName, @Nullable PBXFileReference productReference) {
		this.name = name;
		this.productType = productType;
		this.buildPhases = buildPhases;
		this.buildConfigurationList = buildConfigurationList;
		this.productName = productName;
		this.productReference = productReference;
	}

	public String getName() {
        return name;
    }

    public String getProductType() {
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

    @Nullable
    public PBXFileReference getProductReference() {
        return productReference;
    }

    @Override
    public int stableHash() {
        return name.hashCode();
    }
}
