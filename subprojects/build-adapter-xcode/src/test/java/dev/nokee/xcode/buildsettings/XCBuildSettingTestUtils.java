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
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingNull;
import dev.nokee.xcode.XCBuildSettings;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;

public class XCBuildSettingTestUtils {
	public static XCBuildSetting buildSetting(String name) {
		return buildSetting(name, throwsException(new UnsupportedOperationException()));
	}

	public static XCBuildSetting buildSetting(String name, Function<XCBuildSetting.EvaluationContext, String> evaluate) {
		return new XCBuildSetting() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public String evaluate(EvaluationContext context) {
				return evaluate.apply(context);
			}
		};
	}

	public static Function<XCBuildSetting.EvaluationContext, String> evaluateToNull() {
		return __ -> null;
	}

	public static Function<XCBuildSetting.EvaluationContext, String> evaluateTo(String value) {
		return __ -> value;
	}

	public static Function<XCBuildSetting.EvaluationContext, String> throwsException(RuntimeException throwable) {
		return __ -> { throw throwable; };
	}

	public static Function<XCBuildSetting.EvaluationContext, String> evaluateToNested(String buildSettingName) {
		return context -> context.get(buildSettingName);
	}

	public static Matcher<XCBuildSettingLayer.SearchContext> searchContextForName(String name) {
		return new FeatureMatcher<XCBuildSettingLayer.SearchContext, String>(equalTo(name), "", "") {
			@Override
			protected String featureValueOf(XCBuildSettingLayer.SearchContext actual) {
				return actual.getName();
			}
		};
	}

	public static Map<String, XCBuildSetting> mapOf(XCBuildSetting... buildSettings) {
		return Stream.of(buildSettings).collect(ImmutableMap.toImmutableMap(XCBuildSetting::getName, Function.identity()));
	}

	public static Matcher<XCBuildSetting> nullBuildSetting(String name) {
		return equalTo(new XCBuildSettingNull(name));
	}

	public static XCBuildSettings buildSettings(Consumer<? super ImmutableMap.Builder<String, String>> builderAction) {
		final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		builderAction.accept(builder);
		final Map<String, String> values = builder.build();
		return new XCBuildSettings() {
			@Nullable
			@Override
			public String get(String name) {
				return values.get(name);
			}
		};
	}
}
