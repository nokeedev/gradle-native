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

import dev.nokee.internal.testing.MockitoUtils;
import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.buildSetting;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.buildSettingNamed;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.mapOf;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.nullBuildSetting;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.verify;

class DefaultXCBuildSettingLayerTests {
	XCBuildSetting myVar = buildSetting("MY_VAR");
	XCBuildSetting myOtherVar = buildSetting("MY_OTHER_VAR");
	DefaultXCBuildSettingLayer subject = new DefaultXCBuildSettingLayer(mapOf(myVar, myOtherVar));

	@Test
	void canFindExistingBuildSetting() {
		assertThat(subject.find(buildSettingNamed("MY_VAR")), equalTo(myVar));
	}

	@Test
	void findInTheNextLayerWhenBuildSettingIsNotFound() {
		val context = MockitoUtils.spy(XCBuildSettingLayer.SearchContext.class, buildSettingNamed("MY_MISSING_VAR"));
		assertThat(subject.find(context), nullBuildSetting("MY_MISSING_VAR"));
		verify(context).findNext();
	}

	@Test
	void findAllBuildSettingsKnownToTheLayer() {
		assertThat(subject.findAll(), allOf( //
			hasEntry("MY_VAR", myVar), //
			hasEntry("MY_OTHER_VAR", myOtherVar)));
	}

	@Test
	void canSerialize() {
		assertThat(subject, isSerializable());
	}
}
