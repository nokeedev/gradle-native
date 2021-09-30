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
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ConfigurationSectionBuilder {
	private final List<Section> sections = new ArrayList<>();
	private final String name;
	private final Supplier<Boolean> isCreate;

	ConfigurationSectionBuilder(String name, Supplier<Boolean> isCreate) {
		this.name = name;
		this.isCreate = isCreate;
	}

	public ConfigurationSectionBuilder extendsFrom(String... superConfs) {
		sections.add(new GenericSection(
			() -> "extendsFrom " + String.join(",", superConfs),
			() -> "extendsFrom(" + Arrays.stream(superConfs).map(it -> "configurations.getByName(\"" + it + "\")").collect(Collectors.joining(",")) + ")"
		));
		return this;
	}

	public ConfigurationSectionBuilder canBeConsumed(boolean value) {
		sections.add(new GenericSection(
			() -> "canBeConsumed = " + value,
			() -> "isCanBeConsumed = " + value
		));
		return this;
	}

	public ConfigurationSectionBuilder canBeResolved(boolean value) {
		sections.add(new GenericSection(
			() -> "canBeResolved = " + value,
			() -> "isCanBeResolved = " + value
		));
		return this;
	}

	Section build() {
		return new GenericSection(
			() -> new NamedSection(name, sections).generateSection(GradleDsl.GROOVY),
			() -> {
				val creation = isCreate.get();
				val prefix =  creation ? "val " + name + " by configurations.creating " : name + ".run ";
				return new NamedSection(prefix, sections).generateSection(GradleDsl.KOTLIN);
			}
		);
	}
}
