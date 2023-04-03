/*
 * Copyright 2023 the original author or authors.
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

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@EqualsAndHashCode
public final class DefaultXCBuildSettingLayer implements XCBuildSettingLayer, Serializable {
	private final Map<String, XCBuildSetting> buildSettings;
	private final XCBuildSettingLayer delegate;

	public DefaultXCBuildSettingLayer(Map<String, XCBuildSetting> buildSettings, XCBuildSettingLayer delegate) {
		assert buildSettings != null : "'buildSettings' must not be null";
		assert delegate != null : "'delegate' must not be null";
		this.buildSettings = buildSettings;
		this.delegate = delegate;
	}

	@Override
	public XCBuildSetting find(SearchContext context) {
		assert context != null : "'context' must not be null";
		final XCBuildSetting value = buildSettings.get(context.getName());
		if (value != null) {
			return value;
		} else {
			return delegate.find(context);
		}
	}

	@Override
	public Map<String, XCBuildSetting> findAll() {
		LinkedHashMap<String, XCBuildSetting> result = new LinkedHashMap<>(buildSettings);
		delegate.findAll().forEach((k, v) -> {
			if (!result.containsKey(k)) {
				result.put(k, v);
			}
		});
		return result;
	}
}
