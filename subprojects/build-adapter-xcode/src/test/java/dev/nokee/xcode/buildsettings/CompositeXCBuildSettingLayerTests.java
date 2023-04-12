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
package dev.nokee.xcode.buildsettings;

import dev.nokee.xcode.CompositeXCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSetting;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.buildadapter.xcode.testers.XCBuildSettingTestUtils.buildSetting;
import static dev.nokee.buildadapter.xcode.testers.XCBuildSettingTestUtils.buildSettingNamed;
import static dev.nokee.buildadapter.xcode.testers.XCBuildSettingTestUtils.layerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

class CompositeXCBuildSettingLayerTests {
	XCBuildSetting myVar = buildSetting("MY_VAR");
	XCBuildSetting myOtherVar = buildSetting("MY_OTHER_VAR");
	XCBuildSetting myShadowedVar1 = buildSetting("MY_SHADOWED_VAR"); // different instance
	XCBuildSetting myShadowedVar2 = buildSetting("MY_SHADOWED_VAR"); // different instance

	CompositeXCBuildSettingLayer subject = new CompositeXCBuildSettingLayer(of(layerOf(myVar, myShadowedVar1), layerOf(myOtherVar, myShadowedVar2)));

	@Test
	void canFindBuildSettingsExclusiveToFirstLayer() {
		assertThat(subject.find(buildSettingNamed("MY_VAR")), equalTo(myVar));
	}

	@Test
	void canFindBuildSettingsExclusiveToLowerLayers() {
		assertThat(subject.find(buildSettingNamed("MY_OTHER_VAR")), equalTo(myOtherVar));
	}

	@Test
	void returnsFirstFoundBuildSettingsWhenContainedInMultipleLayers() {
		assertThat(subject.find(buildSettingNamed("MY_SHADOWED_VAR")), equalTo(myShadowedVar1));
	}

	@Test
	void returnsAllOfFirstFoundBuildSettingsFromAllLayers() {
		assertThat(subject.findAll(), allOf(hasEntry("MY_VAR", myVar), hasEntry("MY_OTHER_VAR", myOtherVar),
			hasEntry("MY_SHADOWED_VAR", myShadowedVar1)));
	}

	@Test
	void canSerialize() {
		assertThat(subject, isSerializable());
	}
}
