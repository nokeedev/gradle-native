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
package nokeebuild.shim;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

class MultiGradleVersionPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("nokeebuild.java-gradle-library"); // only because of java compatibility

		sourceSets(project, it -> it.configureEach(new OnlyGradleVersionSourceSet(new AddGradleApiDependencyToCompileOnlyConfiguration(project))));
		sourceSets(project, it -> it.configureEach(new OnlyGradleVersionSourceSet(new AddMainProjectArtifactDependencyToCompileOnlyConfiguration(project))));
		sourceSets(project, it -> it.configureEach(new OnlyGradleVersionSourceSet(new RegisterGradleVersionFeature(project))));
		sourceSets(project, it -> it.named("main", new DependsOnAllGradleVersionSourceSet(project)));
	}

	private static void sourceSets(Project project, Action<? super SourceSetContainer> action) {
		project.getExtensions().configure("sourceSets", action);
	}
}
