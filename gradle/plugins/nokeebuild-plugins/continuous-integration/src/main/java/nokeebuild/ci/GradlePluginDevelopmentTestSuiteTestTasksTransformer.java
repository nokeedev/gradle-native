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
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

class GradlePluginDevelopmentTestSuiteTestTasksTransformer implements Transformer<Provider<? extends Iterable<Test>>, GradlePluginDevelopmentTestSuite> {
	private final GradlePluginDevelopmentTestSuiteTestTasksMapper mapper;

	public GradlePluginDevelopmentTestSuiteTestTasksTransformer(GradlePluginDevelopmentTestSuiteTestTasksMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Provider<? extends Iterable<Test>> transform(GradlePluginDevelopmentTestSuite testSuite) {
		// We flatMap here because of an issue with Gradle not recognizing our intention properly
		//   Gradle thinks we are accessing a Task before its execution...
		//   when in fact we are filtering a set of tasks to use as dependencies
		return mapper.apply(testSuite, testSuite.getTestTasks().getElements().flatMap(Providers::of));
	}
}
