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
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import nokeebuild.testing.strategies.DevelopmentTestingStrategy;
import nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategy;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.testing.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static nokeebuild.UseJUnitJupiter.junitVersion;
import static nokeebuild.UseSpockFramework.spockVersion;
import static nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategies.LINUX;
import static nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategies.MACOS;
import static nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategies.WINDOWS;

abstract /*final*/ class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
	@Inject
	public GradlePluginDevelopmentFunctionalTestingPlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(TestingBasePlugin.class);
		project.getPluginManager().apply("groovy-base");
		project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-functional-test");
		functionalTest(project, new RegisterOperatingSystemFamilyTestingStrategy());
		functionalTest(project, new DisableNonDevelopmentTestTaskOnIdeaSync(project));
		functionalTest(project, testSuite -> {
			testSuite.dependencies(it -> {
				it.implementation(project.project(":internalTesting"));
				it.implementation(it.gradleFixtures());
			});
			testSuite.getTestTasks().configureEach(task -> {
				task.doFirst(new MyAction(project));
			});
		});
		functionalTest(project, new UseJUnitJupiter(junitVersion(project)));
		functionalTest(project, new UseSpockFramework(spockVersion(project)));
		functionalTest(project, testSuite -> {
			final SetProperty<String> testedGradleVersions = project.getObjects().setProperty(String.class);
			final SetProperty<OperatingSystemFamilyTestingStrategy> testedOsFamilies = project.getObjects().setProperty(OperatingSystemFamilyTestingStrategy.class);

			testedGradleVersions.convention(Arrays.asList("minimum", "6.9.2", "latestGA", "latestNightly"));
			testedOsFamilies.convention(majorOperatingSystemFamilies());

			testSuite.getTestingStrategies().value(project.getProviders().zip(testedGradleVersions.map(it -> {
				final GradlePluginTestingStrategyFactory strategyFactory = testSuite.getStrategies();
				return it.stream().map(v -> {
					switch (v) {
						case "minimum": return strategyFactory.getCoverageForMinimumVersion();
						case "latestGA": return strategyFactory.getCoverageForLatestGlobalAvailableVersion();
						case "latestNightly": return strategyFactory.getCoverageForLatestNightlyVersion();
						default: return strategyFactory.coverageForGradleVersion(v);
					}
				}).collect(Collectors.toList());
			}), testedOsFamilies, (versions, osFamilies) -> {
				final Set<GradlePluginTestingStrategy> strategies = new LinkedHashSet<>();
				final GradlePluginTestingStrategyFactory strategyFactory = testSuite.getStrategies();
				osFamilies.forEach(osFamily -> {
					versions.forEach(version -> {
						strategies.add(strategyFactory.composite(osFamily, version));
					});
				});

				final DevelopmentTestingStrategy developmentStrategy = new DevelopmentTestingStrategy();
				versions.forEach(version -> {
					strategies.add(strategyFactory.composite(developmentStrategy, version));
				});
				return strategies;
			})).disallowChanges();

			testSuite.getExtensions().add("testedGradleVersions", testedGradleVersions);
			testSuite.getExtensions().add("testedOsFamilies", testedOsFamilies);
		});
		project.getPluginManager().withPlugin("java-test-fixtures", __ -> {
			functionalTest(project, testSuite -> testSuite.dependencies(it -> it.implementation(it.testFixtures(project))));
		});
	}

	private static final class MyAction implements Action<Task> {
		private final TestKitDirectoryArgumentProvider provider;

		public MyAction(Project project) {
			provider = new TestKitDirectoryArgumentProvider(project);
		}

		@Override
		public void execute(Task task) {
			((Test) task).getJvmArgumentProviders().add(provider);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			return o instanceof TestKitDirectoryArgumentProvider;
		}

		@Override
		public int hashCode() {
			return Objects.hash(getClass());
		}
	}

	private static void functionalTest(Project project, Action<? super GradlePluginDevelopmentTestSuite> action) {
		final GradlePluginDevelopmentTestSuite extension = (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("functionalTest");
		action.execute(extension);
	}

	private static Iterable<OperatingSystemFamilyTestingStrategy> majorOperatingSystemFamilies() {
		return Arrays.asList(WINDOWS, LINUX, MACOS);
	}
}
