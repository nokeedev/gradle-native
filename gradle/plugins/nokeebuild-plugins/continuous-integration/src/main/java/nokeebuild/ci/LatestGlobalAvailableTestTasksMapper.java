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
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.GradlePluginTestingStrategy.Spec.matches;
import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;

class LatestGlobalAvailableTestTasksMapper implements GradlePluginDevelopmentTestSuiteTestTasksMapper {
	@Override
	public Provider<Iterable<Test>> apply(GradlePluginDevelopmentTestSuite testSuite, Provider<? extends Iterable<Test>> testTasks) {
		final GradleVersionCoverageTestingStrategy latestGA = testSuite.getStrategies().getCoverageForLatestGlobalAvailableVersion();
		return testTasks.map(filtered(task -> matches(latestGA::equals).isSatisfiedBy(testingStrategy(task).get()))).flatMap(Providers::of);
	}

    private static <IN> Transformer<Iterable<IN>, Iterable<? extends IN>> filtered(Predicate<? super IN> predicate) {
        return values -> StreamSupport.stream(values.spliterator(), false).filter(predicate).collect(Collectors.toList());
    }
}
