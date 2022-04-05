/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.xcode.internal.xcodeproj;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * The root object representing the project itself.
 */
public final class PBXProject extends PBXContainer {
    private final PBXGroup mainGroup;
    private final List<PBXTarget> targets;
    private final XCConfigurationList buildConfigurationList;
    private final String compatibilityVersion;
    private final String name;

    public PBXProject(String name) {
        this.name = Preconditions.checkNotNull(name);
        this.mainGroup = new PBXGroup("mainGroup", null, PBXReference.SourceTree.GROUP);
        this.targets = Lists.newArrayList();
        this.buildConfigurationList = new XCConfigurationList();
        this.compatibilityVersion = "Xcode 3.2";
    }

    public String getName() {
        return name;
    }

    public PBXGroup getMainGroup() {
        return mainGroup;
    }

    public List<PBXTarget> getTargets() {
        return targets;
    }

    public XCConfigurationList getBuildConfigurationList() {
        return buildConfigurationList;
    }

    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    @Override
    public String isa() {
        return "PBXProject";
    }

    @Override
    public int stableHash() {
        return name.hashCode();
    }

}
