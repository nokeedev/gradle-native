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
import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSetContainer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;

final class UseGradleCoverageAsGradleApiRuntimeDependency implements Action<GradlePluginDevelopmentTestSuite> {
	private final ConfigurationContainer configurations;
	private final GradlePluginDevelopmentDependencyExtension dependencies;
	private final ObjectFactory objects;
	private final SourceSetContainer sourceSets;

	public UseGradleCoverageAsGradleApiRuntimeDependency(Project project) {
		this.configurations = project.getConfigurations();
		this.dependencies = GradlePluginDevelopmentDependencyExtension.from(project.getDependencies());
		this.objects = project.getObjects();
		this.sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
	}

	@Override
	public void execute(GradlePluginDevelopmentTestSuite testSuite) {
		testSuite.getTestTasks().configureEach(task -> {
			final ConfigurableFileCollection testRuntimeClasspath = objects.fileCollection().from(testingStrategy(task).map(strategy -> {

				String version = Objects.requireNonNull(findGradleCoverageVersion(strategy));
				Object result = configurations.create("v" + version + "TestRuntimeClasspath", configuration -> {
					configuration.setCanBeResolved(true);
					configuration.setCanBeConsumed(false);
					configuration.extendsFrom(configurations.getByName("testRuntimeClasspath"));
					configuration.getDependencies().add(dependencies.gradleApi(version));
				});
				return result;
			}).orElse(Collections.emptyList()));
			testRuntimeClasspath.finalizeValueOnRead(); // Note this will erase any task dependencies, but there shouldn't be any
			task.setClasspath(testRuntimeClasspath
				.plus(objects.fileCollection().from((Callable<Object>) () -> sourceSets.getByName("main").getRuntimeClasspath()))
				.plus(sourceSets.getByName("test").getOutput()));
		});
	}

	@Nullable
	private static String findGradleCoverageVersion(GradlePluginTestingStrategy strategy) {
		if (strategy instanceof CompositeGradlePluginTestingStrategy) {
			return StreamSupport.stream(((CompositeGradlePluginTestingStrategy) strategy).spliterator(), false)
				.map(UseGradleCoverageAsGradleApiRuntimeDependency::findGradleCoverageVersion)
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
		} else if (strategy instanceof GradleVersionCoverageTestingStrategy) {
			return ((GradleVersionCoverageTestingStrategy) strategy).getVersion();
		} else {
			return null;
		}
	}
}
