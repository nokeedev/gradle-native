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
package nokeebuild.testing.performance;

import nokeebuild.testing.performance.reporting.PerformanceScenarioXmlReport;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.reporting.Report;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.TestTaskReports;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static nokeebuild.testing.performance.ReportContainerUtils.create;

abstract class GradlePluginDevelopmentPerformanceTestingPlugin implements Plugin<Project> {
	@Inject
	public GradlePluginDevelopmentPerformanceTestingPlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("java-base");

		sourceSets(project, sourceSets -> sourceSets.register("performanceTest"));

		final Provider<SourceSet> performanceTestSourceSet = sourceSets(project).flatMap(it -> it.named("performanceTest"));

		project.getTasks().register("performanceTest", Test.class, task -> {
			task.setGroup("verification");
			task.useJUnitPlatform();
			task.setTestClassesDirs(project.files(performanceTestSourceSet.flatMap(it -> it.getOutput().getClassesDirs().getElements())));
			task.setClasspath(project.files(performanceTestSourceSet.map(SourceSet::getRuntimeClasspathConfigurationName).flatMap(project.getConfigurations()::named), performanceTestSourceSet.flatMap(it -> it.getOutput().getElements())));

			final NamedDomainObjectProvider<PerformanceScenarioXmlReport> performanceReport = create(task.getReports(), "scenarioXml", PerformanceScenarioXmlReport.class, task);
			performanceReport.configure(report -> {
				report.getOutputLocation().convention(project.getLayout().getBuildDirectory().dir("performance-results/" + task.getName()));
			});
			task.getJvmArgumentProviders().add(new PerformanceScenarioXmlReportArgumentProvider(performanceReport));
		});

		// TODO: Use UseJUnitJupiter action once we use GradlePluginDevelopmentTestSuite
		project.getDependencies().add("performanceTestImplementation", project.getDependencies().platform("org.junit:junit-bom:5.8.2"));
		project.getDependencies().add("performanceTestImplementation", "org.junit.jupiter:junit-jupiter");
	}

	private static void sourceSets(Project project, Action<? super SourceSetContainer> action) {
		action.execute((SourceSetContainer) project.getExtensions().getByName("sourceSets"));
	}

	private static Provider<SourceSetContainer> sourceSets(Project project) {
		return project.provider(() -> (SourceSetContainer) project.getExtensions().getByName("sourceSets"));
	}
}
