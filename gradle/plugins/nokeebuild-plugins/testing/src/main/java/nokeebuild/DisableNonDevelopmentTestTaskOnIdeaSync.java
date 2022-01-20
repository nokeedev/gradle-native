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
package nokeebuild;

import dev.gradleplugins.CompositeGradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategy;
import nokeebuild.testing.strategies.DevelopmentTestingStrategy;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;

/**
 * Disable non-development {@literal Test} task on IDE sync.
 * IntelliJ sync uses the {@literal Test} task's {@literal testClassesDirs} to matches with sources.
 * By nullifying {@literal testClassesDirs}, we effectively remove the {@literal Test} task from any suggestion.
 */
final class DisableNonDevelopmentTestTaskOnIdeaSync implements Action<GradlePluginDevelopmentTestSuite> {
	private final Project project;

	public DisableNonDevelopmentTestTaskOnIdeaSync(Project project) {
		this.project = project;
	}

	@Override
	public void execute(GradlePluginDevelopmentTestSuite testSuite) {
		onIdeaSync(project, () -> {
			testSuite.getTestTasks().configureEach(task -> {
				final FileCollection testClassesDirs = task.getTestClassesDirs();
				final Provider<GradlePluginTestingStrategy> testingStrategyProvider = testingStrategy(task);
				task.setTestClassesDirs(project.getObjects().fileCollection().from(callableOf(() -> {
					final GradlePluginTestingStrategy testingStrategy = testingStrategyProvider.getOrNull();
					if (testingStrategy == null) {
						return testClassesDirs;
					} else if (stream(testingStrategy).anyMatch(it -> it instanceof DevelopmentTestingStrategy)) {
						return testClassesDirs;
					} else {
						return Collections.emptyList();
					}
				})));
			});
		});
	}

	private static Stream<GradlePluginTestingStrategy> stream(GradlePluginTestingStrategy strategy) {
		if (strategy instanceof CompositeGradlePluginTestingStrategy) {
			return Stream.concat(Stream.of(strategy), StreamSupport.stream(((CompositeGradlePluginTestingStrategy) strategy).spliterator(), false));
		} else {
			return Stream.of(strategy);
		}
	}

	private static void onIdeaSync(Project project, Runnable action) {
		project.getPluginManager().withPlugin("idea", ignored -> action.run());
	}

	private static <T> Callable<T> callableOf(Callable<T> callable) {
		return callable;
	}
}
