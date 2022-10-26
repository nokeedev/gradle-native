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

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public final class DefaultXCBuildSettingLayer implements XCBuildSettingLayer {
	private final Map<String, XCBuildSettingDefinition> buildSettingsDefinition;

	public DefaultXCBuildSettingLayer(Map<String, XCBuildSettingDefinition> buildSettingsDefinition) {
		this.buildSettingsDefinition = buildSettingsDefinition;
	}


	@Nullable
	@Override
	public XCBuildSetting find(SearchContext context) {
		return Optional.ofNullable(buildSettingsDefinition.get(context.getName())).map(it -> it.select((XCBuildSettingDefinition.SelectContext) context)).orElse(null);
	}
}
