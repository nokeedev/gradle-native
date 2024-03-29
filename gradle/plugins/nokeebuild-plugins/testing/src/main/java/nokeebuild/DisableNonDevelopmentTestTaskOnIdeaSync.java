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

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategy;
import nokeebuild.testing.strategies.DevelopmentTestingStrategy;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;

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
					} else if (matches(DevelopmentTestingStrategy.class::isInstance).isSatisfiedBy(testingStrategy)) {
						return testClassesDirs;
					} else {
						return Collections.emptyList();
					}
				})));
			});
		});
	}

	private Provider<GradlePluginTestingStrategy> testingStrategy(Test task) {
		return project.provider(() -> (Provider<GradlePluginTestingStrategy>) task.getExtensions().findByName("testingStrategy")).flatMap(it -> it);
	}

	private static void onIdeaSync(Project project, Runnable action) {
		// We cannot use project.pluginManager.withPlugin('idea') hook because ...
		//   - Idea plugin seems to be applied after the project is evaluated
		//   - Idea sync resolve testClassesDirs before applying 'idea' plugin
		boolean activeIdea = project.getProviders().systemProperty("idea.sync.active").forUseAtConfigurationTime()
			.map(Boolean::valueOf).orElse(Boolean.FALSE).get();
		if (activeIdea) {
			action.run();
		}
	}

	private static <T> Callable<T> callableOf(Callable<T> callable) {
		return callable;
	}
}
