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

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

class SamplesBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final NamedDomainObjectContainer<Sample> extension = project.getObjects().domainObjectContainer(Sample.class, name -> project.getObjects().newInstance(Sample.class, name, project));
		extension.configureEach(sample -> sample.getArtifacts().withType(SampleArchiveArtifact.class).configureEach(it -> it.getProductVersion().value(sample.getProductVersion()).disallowChanges()));
		project.getExtensions().add("samples", extension);

		project.afterEvaluate(proj -> extension.forEach(Sample::finalizeSample));
	}
}
