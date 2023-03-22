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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.xcode.DefaultXCBuildSetting;
import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingsEmptyLayer;
import dev.nokee.xcode.XCString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static dev.nokee.xcode.XCString.of;
import static dev.nokee.xcode.XCString.variable;
import static java.util.function.Function.identity;

public class KnownBuildSettingsLayerBuilder {
	private static final Map<String, XCString> KNOWN_BUILD_SETTINGS = new HashMap<String, XCString>() {{
		put("PROJECT_TEMP_ROOT", variable("OBJROOT")); // for completeness
		put("PROJECT_TEMP_DIR", of("$(PROJECT_TEMP_ROOT)/$(PROJECT_NAME).build"));
		put("BUILD_DIR", variable("SYMROOT"));
		put("BUILD_ROOT", variable("SYMROOT"));
		put("TARGETNAME", variable("TARGET_NAME")); // for completeness
		// CONFIGURATION_BUILD_DIR may be overwritten per-target
		put("BUILT_PRODUCTS_DIR", variable("CONFIGURATION_BUILD_DIR"));
		put("SOURCE_ROOT", variable("SRCROOT"));
		put("PROJECT_DIR", variable("SRCROOT")); // for completeness
		put("SHARED_PRECOMPS_DIR", of("$(OBJROOT)/PrecompiledHeaders")); // semi-safe assumption
		put("PODS_BUILD_DIR", variable("BUILD_DIR")); // semi-safe assumption
		// PODS_CONFIGURATION_BUILD_DIR may be overwritten per-target
		// PODS_XCFRAMEWORKS_BUILD_DIR may be overwritten per-target
		// PODS_ROOT is almost always declared per-target
	}};
	private XCBuildSettingLayer nextLayer = new XCBuildSettingsEmptyLayer();

	public KnownBuildSettingsLayerBuilder next(XCBuildSettingLayer layer) {
		assert layer != null : "'layer' must not be null";
		this.nextLayer = layer;
		return this;
	}

	public XCBuildSettingLayer build() {
		return new DefaultXCBuildSettingLayer(KNOWN_BUILD_SETTINGS.entrySet().stream().map(toBuildSetting()).collect(toImmutableMap(XCBuildSetting::getName, identity())), nextLayer);
	}

	private static Function<Map.Entry<String, XCString>, XCBuildSetting> toBuildSetting() {
		return entry -> new DefaultXCBuildSetting(entry.getKey(), entry.getValue());
	}
}
