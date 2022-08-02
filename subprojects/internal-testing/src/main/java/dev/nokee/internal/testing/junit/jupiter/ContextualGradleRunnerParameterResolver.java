/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.internal.testing.junit.jupiter;

import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static dev.gradleplugins.runnerkit.GradleExecutor.gradleTestKit;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

public class ContextualGradleRunnerParameterResolver implements ParameterResolver, ExecutionCondition {
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter().getType() == GradleRunner.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		GradleRunner result = GradleRunner.create(gradleTestKit())
			.inDirectory(() -> extensionContext.getStore(GLOBAL).get("temp.dir"))
			.withPluginClasspath()
			.withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion"))
			.beforeExecute(it -> {
				System.out.println("Using Gradle v" + System.getProperty("dev.gradleplugins.defaultGradleVersion") + " in '" + it.getWorkingDirectory().getAbsolutePath() + "'");
				return it;
			});
		if (getGradleVersion().compareTo(VersionNumber.parse("7.6")) >= 0) {
			result = result.withArgument("-Dorg.gradle.kotlin.dsl.precompiled.accessors.strict=true");
		}
		return result;
	}

	private static VersionNumber getGradleVersion() {
		return VersionNumber.parse(System.getProperty("dev.gradleplugins.defaultGradleVersion"));
	}

	private static final ConditionEvaluationResult DEFAULT_REQUIRES_GRADLE_FEATURE_ENABLED = ConditionEvaluationResult.enabled(
		"No @RequiresGradleFeature conditions resulting in 'disabled' execution encountered");
	private static final ConditionEvaluationResult DEFAULT_GRADLE_AT_LEAST_VERSION_ENABLED = ConditionEvaluationResult.enabled(
		"No @GradleAtLeast conditions resulting in 'disabled' execution encountered");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		ConditionEvaluationResult result = findAnnotation(context.getElement(), RequiresGradleFeature.class)
			.map(requirements -> requirements.value().isSupported(getGradleVersion()))
			.orElse(DEFAULT_REQUIRES_GRADLE_FEATURE_ENABLED);

		if (!result.isDisabled()) {
			result = findAnnotation(context.getElement(), GradleAtLeast.class)
				.map(requirements -> isAtLeast(VersionNumber.parse(requirements.value())))
				.orElse(DEFAULT_GRADLE_AT_LEAST_VERSION_ENABLED);
		}

		return result;
	}

	private ConditionEvaluationResult isAtLeast(VersionNumber atLeastVersion) {
		if (atLeastVersion.compareTo(getGradleVersion()) <= 0) {
			return ConditionEvaluationResult.enabled(String.format("%s is at least %s", getGradleVersion(), atLeastVersion));
		} else {
			return ConditionEvaluationResult.disabled(String.format("%s is not at least %s", getGradleVersion(), atLeastVersion));
		}
	}
}
