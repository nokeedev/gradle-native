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

import java.util.List;

public final class DefaultXCBuildSettings implements XCBuildSettings {
	private final List<XCBuildSettingLayer> layers;

	public DefaultXCBuildSettings(List<XCBuildSettingLayer> layers) {
		this.layers = layers;
	}

	@Override
	public String get(String name) {
		for (int i = 0; i < layers.size(); i++) {
			final XCBuildSetting buildSetting = layers.get(i).find(new XCBuildSettingLayer.SearchContext() {
				@Override
				public String getName() {
					return name;
				}
			});
			if (buildSetting != null) {
				return buildSetting.evaluate(new XCBuildSetting.EvaluationContext() {
					@Override
					public String get(String value) {
						if (value.equals(name)) {
							// continue in lower layers
							throw new UnsupportedOperationException("not yet implemented");
						} else {
							return DefaultXCBuildSettings.this.get(name);
						}
					}
				});
			}
		}
		return null;
	}
}
