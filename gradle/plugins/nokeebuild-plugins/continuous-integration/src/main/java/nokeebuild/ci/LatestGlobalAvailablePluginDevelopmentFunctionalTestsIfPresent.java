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
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import java.util.Collections;
import java.util.concurrent.Callable;

final class LatestGlobalAvailablePluginDevelopmentFunctionalTestsIfPresent implements Callable<Object> {
	private final Project project;

	public LatestGlobalAvailablePluginDevelopmentFunctionalTestsIfPresent(Project project) {
		this.project = project;
	}

	@Override
	public Object call() throws Exception {
		if (project.getPluginManager().hasPlugin("nokeebuild.gradle-plugin-functional-test")) {
			return functionalTest(project).flatMap(new GradlePluginDevelopmentTestSuiteTestTasksTransformer(new OperatingSystemFamilyTestTasksMapper().andThen(new LatestGlobalAvailableTestTasksMapper())));
		} else {
			return Collections.emptySet();
		}
	}

	private static Provider<GradlePluginDevelopmentTestSuite> functionalTest(Project project) {
		return project.provider(() -> (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("functionalTest"));
	}
}
