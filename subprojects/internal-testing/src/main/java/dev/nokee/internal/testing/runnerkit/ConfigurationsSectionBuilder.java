/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing.runnerkit;

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class ConfigurationsSectionBuilder {
	private final List<Section> sections = new ArrayList<>();
	private final Set<String> alreadyCreated;

	ConfigurationsSectionBuilder(Set<String> alreadyCreated) {
		this.alreadyCreated = alreadyCreated;
	}

	public ConfigurationsSectionBuilder withConfiguration(String name, Consumer<? super ConfigurationSectionBuilder> configurationAction) {
		val builder = new ConfigurationSectionBuilder(name, () -> alreadyCreated.add(name));
		configurationAction.accept(builder);
		sections.add(builder.build());
		return this;
	}

	NamedSection build() {
		return new NamedSection("configurations", sections);
	}
}
