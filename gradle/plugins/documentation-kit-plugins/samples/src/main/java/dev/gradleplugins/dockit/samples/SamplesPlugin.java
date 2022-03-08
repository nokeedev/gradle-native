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
package dev.gradleplugins.dockit.samples;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

class SamplesPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.gradleplugins.documentation.samples-base");
		samples(project, samples -> {
			samples.configureEach(new UseSampleNameAsPermalinkConvention());
			samples.configureEach(new UseBothGradleDslsConvention());
			samples.configureEach(new UseProjectVersionAsProductVersionConvention(project));
			samples.configureEach(new UseSampleNameAsArchiveBaseNameConvention());
		});
		samples(project, new CreateAssembleSamplesTask(project));
		samples(project, new CreateAssembleSampleZipsTask(project));
	}

	private static void samples(Project project, Action<? super NamedDomainObjectContainer<Sample>> action) {
		action.execute((NamedDomainObjectContainer<Sample>) project.getExtensions().getByName("samples"));
	}
}
