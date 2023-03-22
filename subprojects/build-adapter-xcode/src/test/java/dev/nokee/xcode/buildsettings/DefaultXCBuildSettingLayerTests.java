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

import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.DefaultXCBuildSettingSearchContext;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingsEmptyLayer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.buildSetting;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.mapOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

class DefaultXCBuildSettingLayerTests {
	XCBuildSetting myVar = buildSetting("MY_VAR");
	XCBuildSetting myOtherVar = buildSetting("MY_OTHER_VAR");

	@Nested
	class GivenMultipleLayerContainingBuildSettings {
		DefaultXCBuildSettingLayer subject = new DefaultXCBuildSettingLayer(mapOf(myVar), new DefaultXCBuildSettingLayer(mapOf(myOtherVar), new XCBuildSettingsEmptyLayer()));

		@Test
		void findsOnLowerLayerWhenBuildSettingNotFound() {
			assertThat(subject.find(new DefaultXCBuildSettingSearchContext("MY_OTHER_VAR")), equalTo(myOtherVar));
		}

		@Test
		void returnsFoundBuildSetting() {
			assertThat(subject.find(new DefaultXCBuildSettingSearchContext("MY_VAR")), equalTo(myVar));
		}

		@Test
		void hasBuildSettingsFromThisAndNextLayer() {
			assertThat(subject.findAll(), allOf( //
				hasEntry("MY_VAR", myVar), //
				hasEntry("MY_OTHER_VAR", myOtherVar)));
		}
	}

	@Nested
	class GivenLowerLayersContainsDuplicatedBuildSettings {
		XCBuildSetting myVarDuplicated = buildSetting(myVar.getName());
		DefaultXCBuildSettingLayer subject = new DefaultXCBuildSettingLayer(mapOf(myVar), new DefaultXCBuildSettingLayer(mapOf(myVarDuplicated, myOtherVar), new XCBuildSettingsEmptyLayer()));

		@Test
		void returnsFirstBuildSettingFound() {
			assertThat(subject.find(new DefaultXCBuildSettingSearchContext("MY_VAR")), equalTo(myVar));
		}

		@Test
		void includeOnlyHigherLevelBuildSettings() {
			assertThat(subject.findAll(), allOf( //
				hasEntry("MY_VAR", myVar), //
				hasEntry("MY_OTHER_VAR", myOtherVar)));
		}
	}

//	@Test
//	void alwaysForwardsSearchContextToLowerLayers() {
//		val context = new DefaultXCBuildSettingSearchContext("SOME_VAR");
//		val lowerLayer = newSpy(XCBuildSettingLayer.class, new XCBuildSettingsEmptyLayer());
//		val subject = new DefaultXCBuildSettingLayer(ImmutableMap.of(), lowerLayer.instance());
//
//		subject.find(context);
//
//		assertThat(lowerLayer.to(method(XCBuildSettingLayer::find)), calledOnceWith(sameInstance(context)));
//	}
}
