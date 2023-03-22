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

import dev.nokee.xcode.XCBuildSetting;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;

class KnownBuildSettingsLayerBuilderTests {
	KnownBuildSettingsLayerBuilder subject = new KnownBuildSettingsLayerBuilder();
	Map<String, XCBuildSetting> result = subject.build().findAll();

	@Test
	void hasProjectTempRoot() {
		assertThat(result, hasBuildSetting("PROJECT_TEMP_ROOT", "$(OBJROOT)"));
	}

	@Test
	void hasProjectTempDir() {
		assertThat(result, hasBuildSetting("PROJECT_TEMP_DIR", "$(PROJECT_TEMP_ROOT)/$(PROJECT_NAME).build"));
	}

	@Test
	void hasBuildDir() {
		assertThat(result, hasBuildSetting("BUILD_DIR", "$(SYMROOT)"));
	}

	@Test
	void hasBuildRoot() {
		assertThat(result, hasBuildSetting("BUILD_ROOT", "$(SYMROOT)"));
	}

	@Test
	void hasTargetname() {
		assertThat(result, hasBuildSetting("TARGETNAME", "$(TARGET_NAME)"));
	}

	@Test
	void hasNoConfigurationBuildDir() {
		assertThat(result, not(hasKey("CONFIGURATION_BUILD_DIR")));
	}

	@Test
	void hasBuiltProductsDir() {
		assertThat(result, hasBuildSetting("BUILT_PRODUCTS_DIR", "$(CONFIGURATION_BUILD_DIR)"));
	}

	@Test
	void hasSourceRoot() {
		assertThat(result, hasBuildSetting("SOURCE_ROOT", "$(SRCROOT)"));
	}

	@Test
	void hasProjectDir() {
		assertThat(result, hasBuildSetting("PROJECT_DIR", "$(SRCROOT)"));
	}

	@Test
	void hasSharedPrecompsDir() {
		assertThat(result, hasBuildSetting("SHARED_PRECOMPS_DIR", "$(OBJROOT)/PrecompiledHeaders"));
	}

	@Test
	void hasPodsBuildDir() {
		assertThat(result, hasBuildSetting("PODS_BUILD_DIR", "$(BUILD_DIR)"));
	}

	@Test
	void hasNoPodsConfigurationBuildDir() {
		assertThat(result, not(hasKey("PODS_CONFIGURATION_BUILD_DIR")));
	}

	@Test
	void hasNoPodsXcframeworksBuildDir() {
		assertThat(result, not(hasKey("PODS_XCFRAMEWORKS_BUILD_DIR")));
	}

	@Test
	void hasNoPodsRoot() {
		assertThat(result, not(hasKey("PODS_ROOT")));
	}

	private static Matcher<Map<? extends String, ? extends XCBuildSetting>> hasBuildSetting(String name, String value) {
		return hasEntry(equalTo(name), allOf(named(name), hasToString(value)));
	}
}
