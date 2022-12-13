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
package dev.nokee.xcode.objects.configuration;

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.project.CodeableXCConfigurationList;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;
import static java.util.Objects.requireNonNull;

/**
 * List of build configurations.
 */
public interface XCConfigurationList extends PBXProjectItem {
	Map<String, XCBuildConfiguration> getBuildConfigurationsByName();

	List<XCBuildConfiguration> getBuildConfigurations();

	Optional<String> getDefaultConfigurationName();

	boolean isDefaultConfigurationIsVisible();

	static Builder builder() {
		return new Builder();
	}

	Builder toBuilder();

	final class Builder implements org.apache.commons.lang3.builder.Builder<XCConfigurationList>, LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "XCConfigurationList");
			builder.requires(key(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible));
		}

		public Builder(KeyedObject parent) {
			builder.put(KeyedCoders.ISA, "XCConfigurationList");
			builder.parent(parent);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder buildConfiguration(Consumer<? super XCBuildConfiguration.Builder> builderConsumer) {
			final XCBuildConfiguration.Builder builder = XCBuildConfiguration.builder();
			builderConsumer.accept(builder);
			return buildConfiguration(builder.build());
		}

		public Builder buildConfiguration(XCBuildConfiguration buildConfiguration) {
			builder.add(CodeableXCConfigurationList.CodingKeys.buildConfigurations, buildConfiguration);
			return this;
		}

		public Builder buildConfigurations(Iterable<? extends XCBuildConfiguration> buildConfigurations) {
			builder.put(CodeableXCConfigurationList.CodingKeys.buildConfigurations, ImmutableSet.copyOf(buildConfigurations));
			return this;
		}

		public Builder defaultConfigurationName(String name) {
			builder.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, requireNonNull(name));
			return this;
		}

		public Builder defaultConfigurationVisibility(DefaultConfigurationVisibility visibility) {
			builder.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, visibility == DefaultConfigurationVisibility.VISIBLE);
			return this;
		}

		@Override
		public XCConfigurationList build() {
			return new CodeableXCConfigurationList(builder.build());
		}
	}

	enum DefaultConfigurationVisibility {
		HIDDEN,
		VISIBLE;
	}
}
