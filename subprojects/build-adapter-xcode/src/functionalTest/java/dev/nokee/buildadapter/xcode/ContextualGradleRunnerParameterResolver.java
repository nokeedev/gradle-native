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
package dev.nokee.buildadapter.xcode;

import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import static dev.gradleplugins.runnerkit.GradleExecutor.gradleTestKit;

public class ContextualGradleRunnerParameterResolver implements ParameterResolver {
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter().getType() == GradleRunner.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return GradleRunner.create(gradleTestKit()).inDirectory(() -> extensionContext.getStore(ExtensionContext.Namespace.GLOBAL).get("temp.dir")).withPluginClasspath().withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).beforeExecute(it -> {
			System.out.println("Using Gradle v" + System.getProperty("dev.gradleplugins.defaultGradleVersion") + " in '" + it.getWorkingDirectory().getAbsolutePath() + "'");
			return it;
		});
	}
}
