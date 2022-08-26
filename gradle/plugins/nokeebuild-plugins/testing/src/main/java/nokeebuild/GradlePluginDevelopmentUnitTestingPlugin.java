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
import static nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategies.*;

abstract /*final*/ class GradlePluginDevelopmentUnitTestingPlugin implements Plugin<Project> {
	@Inject
	public GradlePluginDevelopmentUnitTestingPlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(TestingBasePlugin.class);
		project.getPluginManager().apply("groovy-base");
		project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-unit-test");
		test(project, new RegisterOperatingSystemFamilyTestingStrategy());
		test(project, new TestingStrategiesConvention());
		test(project, new DisableNonDevelopmentTestTaskOnIdeaSync(project));
		test(project, testSuite -> {
			testSuite.dependencies(it -> {
				it.implementation(project.project(":internalTesting"));
				it.implementation(it.groovy());
			});
		});
		test(project, new UseJUnitJupiter(junitVersion(project)));
		test(project, new UseSpockFramework(spockVersion(project)));
		test(project, new UseGradleCoverageAsGradleApiRuntimeDependency(project));
	}

	private static void test(Project project, Action<? super GradlePluginDevelopmentTestSuite> action) {
		final GradlePluginDevelopmentTestSuite extension = (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("test");
		action.execute(extension);
	}

	private static final class TestingStrategiesConvention implements Action<GradlePluginDevelopmentTestSuite> {
		@Override
		public void execute(GradlePluginDevelopmentTestSuite testSuite) {
			final Set<GradlePluginTestingStrategy> strategies = new LinkedHashSet<>();
			majorOperatingSystemFamilies().forEach(strategies::add);
			strategies.add(new DevelopmentTestingStrategy());
			testSuite.getTestingStrategies().convention(strategies);
		}

		private static Stream<OperatingSystemFamilyTestingStrategy> majorOperatingSystemFamilies() {
			return Stream.of(WINDOWS, LINUX, MACOS);
		}
	}
}
