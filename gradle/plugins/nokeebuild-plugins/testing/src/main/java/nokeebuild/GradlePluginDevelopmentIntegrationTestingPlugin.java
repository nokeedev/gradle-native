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
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.GradlePluginTestingStrategy;
import nokeebuild.testing.strategies.DevelopmentTestingStrategy;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static nokeebuild.UseJUnitJupiter.junitVersion;
import static nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategyFactory.osFamilies;

abstract /*final*/ class GradlePluginDevelopmentIntegrationTestingPlugin implements Plugin<Project> {
	@Inject
	public GradlePluginDevelopmentIntegrationTestingPlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(TestingBasePlugin.class);
		project.getPluginManager().apply("java-base");
		project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");

		final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
		final GradlePluginDevelopmentTestSuite integrationTest = factory.create("integrationTest");
		integrationTest.getSourceSet().disallowChanges();
		project.getExtensions().add("integrationTest", integrationTest);

		project.afterEvaluate(proj -> integrationTest.finalizeComponent());

		integrationTest(project, new RegisterOperatingSystemFamilyTestingStrategy());
		integrationTest(project, new DisableNonDevelopmentTestTaskOnIdeaSync(project));
		integrationTest(project, testSuite -> {
			project.getTasks().named("check", task -> task.dependsOn(testSuite.getTestTasks().getElements()));
		});
		integrationTest(project, new ExtendsFrom(project, sourceSets(project).getByName("main")));
		integrationTest(project, new TestingStrategiesConvention());
		integrationTest(project, testSuite -> {
			testSuite.dependencies(it -> {
				it.implementation(project);
				it.implementation(project.project(":internalTesting"));
				it.implementation(it.groovy()); // ???
			});
		});
		integrationTest(project, new UseJUnitJupiter(junitVersion(project)));
		project.getPluginManager().withPlugin("java-test-fixtures", __ -> {
			integrationTest(project, testSuite -> testSuite.dependencies(it -> it.implementation(it.testFixtures(project))));
		});
	}

	private static void integrationTest(Project project, Action<? super GradlePluginDevelopmentTestSuite> action) {
		final GradlePluginDevelopmentTestSuite extension = (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("integrationTest");
		action.execute(extension);
	}

	private static SourceSetContainer sourceSets(Project project) {
		return project.getExtensions().getByType(SourceSetContainer.class);
	}

	private static final class TestingStrategiesConvention implements Action<GradlePluginDevelopmentTestSuite> {
		@Override
		public void execute(GradlePluginDevelopmentTestSuite testSuite) {
			final Set<GradlePluginTestingStrategy> strategies = new LinkedHashSet<>();
			strategies.add(testSuite.getStrategies().composite(new DevelopmentTestingStrategy(), osFamilies(testSuite).getAgnostic()));
			testSuite.getTestingStrategies().convention(strategies);
		}
	}
}
