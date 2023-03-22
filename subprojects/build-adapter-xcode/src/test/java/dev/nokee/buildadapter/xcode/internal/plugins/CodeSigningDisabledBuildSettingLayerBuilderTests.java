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
import static org.hamcrest.Matchers.hasToString;

class CodeSigningDisabledBuildSettingLayerBuilderTests {
	CodeSigningDisabledBuildSettingLayerBuilder subject = new CodeSigningDisabledBuildSettingLayerBuilder();
	Map<String, XCBuildSetting> result = subject.build().findAll();

	@Test
	void hasCodeSignIdentity() {
		assertThat(result, hasBuildSetting("CODE_SIGN_IDENTITY", ""));
	}

	@Test
	void hasCodeSigningRequired() {
		assertThat(result, hasBuildSetting("CODE_SIGNING_REQUIRED", "NO"));
	}

	@Test
	void hasCodeSignEntitlements() {
		assertThat(result, hasBuildSetting("CODE_SIGN_ENTITLEMENTS", ""));
	}

	@Test
	void hasCodeSigningAllowed() {
		assertThat(result, hasBuildSetting("CODE_SIGNING_ALLOWED", "NO"));
	}

	private static Matcher<Map<? extends String, ? extends XCBuildSetting>> hasBuildSetting(String name, String value) {
		return hasEntry(equalTo(name), allOf(named(name), hasToString(value)));
	}
}
