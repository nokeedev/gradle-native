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

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.DefaultXCBuildSettings;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingNull;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.buildSetting;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.evaluateTo;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.evaluateToNested;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.evaluateToNull;
import static dev.nokee.xcode.buildsettings.XCBuildSettingTestUtils.throwsException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultXCBuildSettingsTests {
	TestLayer layer = new TestLayer();
	DefaultXCBuildSettings subject = new DefaultXCBuildSettings(layer);

	@Test
	void returnsNullWhenRequestedBuildSettingNotFound() {
		assertThat(subject.get("MISSING"), nullValue());
	}

	@Test
	void forwardsEvaluationOfNestedBuildSetting() {
		layer.add(buildSetting("MY_NESTED", evaluateToNested("NESTED")));
		layer.add(buildSetting("NESTED", evaluateTo("nested-value")));

		assertThat(subject.get("MY_NESTED"), equalTo("nested-value"));
	}

	@Test
	void forwardsEvaluationExceptionToCaller() {
		val expectedException = new RuntimeException("expected");
		layer.add(buildSetting("THROWING_RESOLVING", throwsException(expectedException)));

		val ex = assertThrows(RuntimeException.class, () -> subject.get("THROWING_RESOLVING"));
		assertThat(ex, equalTo(expectedException));
	}

	@Test
	void forwardsEvaluationOfNullResolvingBuildSetting() {
		layer.add(buildSetting("NULL_RESOLVING", evaluateToNull()));

		assertThat(subject.get("NULL_RESOLVING"), nullValue());
	}

	@Test
	void forwardsEvaluationOfFoundBuildSetting() {
		layer.add(buildSetting("EXISTING", evaluateTo("foo")));

		assertThat(subject.get("EXISTING"), equalTo("foo"));
	}

	private static final class TestLayer implements XCBuildSettingLayer {
		private final List<XCBuildSetting> buildSettings = new ArrayList<>();

		public void add(XCBuildSetting buildSetting) {
			buildSettings.add(buildSetting);
		}

		@Override
		public XCBuildSetting find(SearchContext context) {
			return buildSettings.stream().filter(it -> it.getName().equals(context.getName())).findFirst().orElse(new XCBuildSettingNull(context.getName()));
		}

		@Override
		public Map<String, XCBuildSetting> findAll() {
			return buildSettings.stream().collect(ImmutableMap.toImmutableMap(XCBuildSetting::getName, Function.identity()));
		}
	};
}
