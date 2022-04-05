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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The root object representing the project itself.
 */
public final class PBXProject extends PBXContainer {
    private final PBXGroup mainGroup;
    private final List<PBXTarget> targets;
    private final XCConfigurationList buildConfigurationList;
    private final String compatibilityVersion;
    private final String name;

	private PBXProject(String name, ImmutableList<PBXTarget> targets, XCConfigurationList buildConfigurationList) {
		this.name = name;
		this.mainGroup = new PBXGroup("mainGroup", null, PBXReference.SourceTree.GROUP);
		this.targets = Lists.newArrayList(targets);
		this.buildConfigurationList = buildConfigurationList;
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
    public int stableHash() {
        return name.hashCode();
    }

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private final ImmutableList.Builder<PBXTarget> targets = ImmutableList.builder();
		private XCConfigurationList buildConfigurations;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder target(PBXTarget target) {
			targets.add(target);
			return this;
		}

		public Builder buildConfigurations(Consumer<? super XCConfigurationList.Builder> builderConsumer) {
			final XCConfigurationList.Builder builder = XCConfigurationList.builder();
			builderConsumer.accept(builder);
			this.buildConfigurations = builder.build();
			return this;
		}

		public PBXProject build() {
			return new PBXProject(Objects.requireNonNull(name), targets.build(), buildConfigurations);
		}
	}
}
