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
package dev.nokee.xcode;

import java.util.Map;

public final class ConditionalBuildSettingDefinitions implements XCBuildSettingDefinition {
	private final String name;
	private final Map<Condition, XCBuildSetting> specs;

	public ConditionalBuildSettingDefinitions(String name, Map<Condition, XCBuildSetting> specs) {
		this.name = name;
		this.specs = specs;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public XCBuildSetting select(SelectContext context) {
		return specs.entrySet().stream().filter(it -> it.getKey().test(context)).findFirst().map(Map.Entry::getValue).orElse(null);
	}

	interface Condition {
		boolean test(SelectContext context);
	}
}
