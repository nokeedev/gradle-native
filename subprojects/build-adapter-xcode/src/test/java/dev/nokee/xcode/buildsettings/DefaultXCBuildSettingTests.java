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

import dev.nokee.xcode.DefaultXCBuildSetting;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCString;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DefaultXCBuildSettingTests {
	@Test
	void canEvaluateLiteralString() {
		val subject = new DefaultXCBuildSetting("MY_VAR", XCString.literal("some value"));

		assertThat(subject.evaluate(newAlwaysThrowingMock(XCBuildSetting.EvaluationContext.class)), equalTo("some value"));
	}

	@Test
	void canEvaluateNestedVariableString() {
		val subject = new DefaultXCBuildSetting("MY_VAR", XCString.variable("MY_OTHER_VAR"));

		assertThat(subject.evaluate(contextOf("MY_OTHER_VAR", "other value")), equalTo("other value"));
	}

	private static XCBuildSetting.EvaluationContext contextOf(String key, String value) {
		return new XCBuildSetting.EvaluationContext() {
			@Nullable
			@Override
			public String get(String name) {
				if (key.equals(name)) {
					return value;
				} else {
					return null;
				}
			}
		};
	}
}
