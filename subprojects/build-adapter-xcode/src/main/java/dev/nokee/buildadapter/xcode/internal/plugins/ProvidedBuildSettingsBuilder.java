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

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.ProvidedMapAdapter;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingLiteral;
import dev.nokee.xcode.XCBuildSettingsEmptyLayer;
import dev.nokee.xcode.XCTargetReference;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@SuppressWarnings("UnstableApiUsage")
public final class ProvidedBuildSettingsBuilder {
	private final MapProperty<String, String> buildSettings;
	private XCBuildSettingLayer nextLayer = new XCBuildSettingsEmptyLayer();

	public ProvidedBuildSettingsBuilder(ObjectFactory objects) {
		assert objects != null : "'objects' must not be null";
		buildSettings = objects.mapProperty(String.class, String.class);
	}

	public ProvidedBuildSettingsBuilder next(XCBuildSettingLayer layer) {
		assert layer != null : "'layer' must not be null";
		this.nextLayer = layer;
		return this;
	}

	public ProvidedBuildSettingsBuilder derivedDataPath(Provider<Path> location) {
		assert location != null : "'location' must not be null";
		buildSettings.put("OBJROOT", location.map(it -> it.resolve("Build/Intermediates.noindex").toString()));
		buildSettings.put("SYMROOT", location.map(it -> it.resolve("Build/Products").toString()));
		return this;
	}

	public ProvidedBuildSettingsBuilder targetReference(Provider<XCTargetReference> reference) {
		assert reference != null : "'reference' must not be null";
		buildSettings.put("PROJECT_NAME", reference.map(it -> it.getProject().getName()));
		buildSettings.put("TARGET_NAME", reference.map(it -> it.getName()));
		buildSettings.put("SRCROOT", reference.map(it -> it.getProject().getLocation().getParent().toString()));
		return this;
	}

	public ProvidedBuildSettingsBuilder configuration(Provider<String> configuration) {
		assert configuration != null : "'configuration' must not be null";
		buildSettings.put("CONFIGURATION", configuration);
		return this;
	}

	public ProvidedBuildSettingsBuilder platformName(Provider<String> sdk) {
		assert sdk != null : "'sdk' must not be null";
		// fallthrough to the next layer, if absent
		buildSettings.putAll(sdk.map(it -> ImmutableMap.of("PLATFORM_NAME", it)).orElse(ImmutableMap.of()));
		return this;
	}

	public ProvidedBuildSettingsBuilder developerDir(Provider<Path> developerDir) {
		assert developerDir != null : "'developerDir' must not be null";
		// We can reasonably guess the DEVELOPER_DIR from the Xcode installation
		buildSettings.put("DEVELOPER_DIR", developerDir.map(Path::toString));
		return this;
	}

	public XCBuildSettingLayer build() {
		return new DefaultXCBuildSettingLayer(new ProvidedMapAdapter<>(buildSettings.map(it -> {
			final Map<String, XCBuildSetting> result = it.entrySet().stream().collect(toMap(Map.Entry::getKey, toBuildSetting()));
			if (it.containsKey("DEVELOPER_DIR") && it.containsKey("PLATFORM_NAME")) {
				final String subPath = sdkPath(it.get("PLATFORM_NAME"));
				if (subPath != null) {
					result.put("SDKROOT", new XCBuildSettingLiteral("SDKROOT", it.get("DEVELOPER_DIR") + "/" + subPath));
				}
			}
			return result;
		})), nextLayer);
	}

	@Nullable
	private static String sdkPath(String platformName) {
		switch (platformName.toLowerCase(Locale.ENGLISH)) {
			case "iphoneos":
				return "Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk";
			case "macosx":
				return "Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk";
			case "iphonesimulator":
				return "Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk";
			default: return null; // nullify to force a fallthrough if SDK doesn't match known values
		}
	}

	private static Function<Map.Entry<String, String>, XCBuildSetting> toBuildSetting() {
		return entry -> new XCBuildSettingLiteral(entry.getKey(), entry.getValue());
	}
}
