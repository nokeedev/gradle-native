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

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

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
		functionalTest(project, new TestingStrategiesConvention());
		functionalTest(project, new DisableNonDevelopmentTestTaskOnIdeaSync(project));
		functionalTest(project, testSuite -> {
			testSuite.dependencies(it -> {
				it.implementation(project.project(":internalTesting"));
				it.implementation(it.gradleFixtures());
			});
			testSuite.getTestTasks().configureEach(task -> {
				task.getJvmArgumentProviders().add(new TestKitDirectoryArgumentProvider(project));
			});
		});
		functionalTest(project, new UseJUnitJupiter(junitVersion(project)));
		functionalTest(project, new UseSpockFramework(spockVersion(project)));
	}

	private static void functionalTest(Project project, Action<? super GradlePluginDevelopmentTestSuite> action) {
		final GradlePluginDevelopmentTestSuite extension = (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("functionalTest");
		action.execute(extension);
	}

	private static final class TestingStrategiesConvention implements Action<GradlePluginDevelopmentTestSuite> {
		@Override
		public void execute(GradlePluginDevelopmentTestSuite testSuite) {
			final Set<GradlePluginTestingStrategy> strategies = new LinkedHashSet<>();
			final GradlePluginTestingStrategyFactory strategyFactory = testSuite.getStrategies();
			majorOperatingSystemFamilies().forEach(osFamily -> {
				strategies.add(strategyFactory.composite(osFamily, strategyFactory.getCoverageForMinimumVersion()));
				strategies.add(strategyFactory.composite(osFamily, strategyFactory.getCoverageForLatestGlobalAvailableVersion()));
				strategies.add(strategyFactory.composite(osFamily, strategyFactory.getCoverageForLatestNightlyVersion()));
			});

			final DevelopmentTestingStrategy developmentStrategy = new DevelopmentTestingStrategy();
			strategies.add(strategyFactory.composite(developmentStrategy, strategyFactory.getCoverageForMinimumVersion()));
			strategies.add(strategyFactory.composite(developmentStrategy, strategyFactory.getCoverageForLatestGlobalAvailableVersion()));
			strategies.add(strategyFactory.composite(developmentStrategy, strategyFactory.getCoverageForLatestNightlyVersion()));
			testSuite.getTestingStrategies().convention(strategies);
		}

		private static Stream<OperatingSystemFamilyTestingStrategy> majorOperatingSystemFamilies() {
			return Stream.of(WINDOWS, LINUX, MACOS);
		}
	}
}
