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

package nokeebuild.ci;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategy;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.testing.Test;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

final class LatestGlobalAvailablePluginDevelopmentFunctionalTestsIfPresent implements Callable<Object> {
	private final Project project;

	public LatestGlobalAvailablePluginDevelopmentFunctionalTestsIfPresent(Project project) {
		this.project = project;
	}

	@Override
	public Object call() throws Exception {
		if (project.getPluginManager().hasPlugin("nokeebuild.gradle-plugin-functional-test")) {
			return functionalTest(project).getTestTasks().getElements().map(matching(latestGlobalAvailableTestingStrategy()));
		} else {
			return Collections.emptySet();
		}
	}

	private Predicate<Test> latestGlobalAvailableTestingStrategy() {
		return task -> testingStrategy(task).get().equals(functionalTest(project).getStrategies().getCoverageForLatestGlobalAvailableVersion());
	}

	private static GradlePluginDevelopmentTestSuite functionalTest(Project project) {
		return (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("functionalTest");
	}

	@SuppressWarnings("unchecked")
	private static Property<GradlePluginTestingStrategy> testingStrategy(Test task) {
		return (Property<GradlePluginTestingStrategy>) task.getExtensions().getByName("testingStrategy");
	}

	private static <T> Transformer<Iterable<T>, Iterable<T>> matching(Predicate<? super T> predicate) {
		return it -> StreamSupport.stream(it.spliterator(), false).filter(predicate).collect(toList());
	}
}
