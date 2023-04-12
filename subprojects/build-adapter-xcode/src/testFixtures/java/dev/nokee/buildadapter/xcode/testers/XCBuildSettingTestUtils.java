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
package dev.nokee.buildadapter.xcode.testers;

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.DefaultXCBuildSetting;
import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.DefaultXCBuildSettingSearchContext;
import dev.nokee.xcode.DefaultXCBuildSettings;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingNull;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCString;
import lombok.EqualsAndHashCode;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;

public class XCBuildSettingTestUtils {
	public static XCBuildSetting buildSetting(String name) {
		return buildSetting(name, throwsException(new TestException()));
	}

	public static XCBuildSetting buildSetting(String name, Function<XCBuildSetting.EvaluationContext, String> evaluate) {
		return new TestXCBuildSetting(name, evaluate);
	}

	public static XCBuildSettingLayer layerOf(XCBuildSetting... buildSettings) {
		return new DefaultXCBuildSettingLayer(mapOf(buildSettings));
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class TestException extends UnsupportedOperationException {}

	@EqualsAndHashCode
	private static final class TestXCBuildSetting implements XCBuildSetting, Serializable {
		private final String name;
		private final Function<XCBuildSetting.EvaluationContext, String> evaluate;

		private TestXCBuildSetting(String name, Function<EvaluationContext, String> evaluate) {
			this.name = name;
			this.evaluate = evaluate;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String evaluate(EvaluationContext context) {
			return evaluate.apply(context);
		}
	}

	public static Function<XCBuildSetting.EvaluationContext, String> evaluateToNull() {
		return (Serializable & Function<XCBuildSetting.EvaluationContext, String>) __ -> null;
	}

	public static Function<XCBuildSetting.EvaluationContext, String> evaluateTo(String value) {
		return (Serializable & Function<XCBuildSetting.EvaluationContext, String>) __ -> value;
	}

	public static Function<XCBuildSetting.EvaluationContext, String> throwsException(RuntimeException throwable) {
		return new ThrowingExceptionEvaluation(throwable);
	}

	@EqualsAndHashCode
	private static final class ThrowingExceptionEvaluation implements Function<XCBuildSetting.EvaluationContext, String>, Serializable {
		private final RuntimeException throwable;

		private ThrowingExceptionEvaluation(RuntimeException throwable) {
			this.throwable = throwable;
		}

		@Override
		public String apply(XCBuildSetting.EvaluationContext evaluationContext) {
			throw throwable;
		}
	}

	public static Function<XCBuildSetting.EvaluationContext, String> evaluateToNested(String buildSettingName) {
		return (Serializable & Function<XCBuildSetting.EvaluationContext, String>) context -> context.get(buildSettingName);
	}

	public static Matcher<XCBuildSettingLayer.SearchContext> searchContextForName(String name) {
		return new FeatureMatcher<XCBuildSettingLayer.SearchContext, String>(equalTo(name), "", "") {
			@Override
			protected String featureValueOf(XCBuildSettingLayer.SearchContext actual) {
				return actual.getName();
			}
		};
	}

	public static XCBuildSettingLayer.SearchContext buildSettingNamed(String name) {
		return new DefaultXCBuildSettingSearchContext(name);
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
		return new DefaultXCBuildSettings(new DefaultXCBuildSettingLayer(values.entrySet().stream().map(it -> new DefaultXCBuildSetting(it.getKey(), XCString.of(it.getValue()))).collect(ImmutableMap.toImmutableMap(XCBuildSetting::getName, Function.identity()))));
	}

	public static Matcher<XCBuildSettings> hasBuildSetting(String name, String resolved) {
		return new FeatureMatcher<XCBuildSettings, String>(equalTo(resolved), "", "") {
			@Override
			protected String featureValueOf(XCBuildSettings actual) {
				return actual.get(name);
			}
		};
	}
}
