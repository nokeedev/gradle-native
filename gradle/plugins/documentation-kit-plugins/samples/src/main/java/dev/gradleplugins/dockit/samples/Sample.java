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

import dev.gradleplugins.dockit.common.TaskNameFactory;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class Sample implements Named, SampleSummary, ExtensionAware, TaskNameFactory {
	private final Names names;
	private final String name;
	private final DomainObjectSet<? super SampleArtifact> artifacts;
	private final Project project;

	@Inject
	public Sample(String name, Project project) {
		this.name = name;
		this.artifacts = project.getObjects().domainObjectSet(SampleArtifact.class);
		this.project = project;
		this.names = new Names(name);
		getExtensions().add("sourceDirectory", getSampleDirectory());
	}

	@Override
	public String getName() {
		return name;
	}

	public abstract DirectoryProperty getSampleDirectory();

	public abstract Property<String> getProductVersion();

	public String taskName(String name) {
		return names.taskName(name);
	}

	public String taskName(String verb, String object) {
		return names.taskName(verb, object);
	}

	public String artifactName(String name) {
		return names.artifactName(name);
	}

	public DomainObjectSet<? super SampleArtifact> getArtifacts() {
		return artifacts;
	}

	public void finalizeSample() {
		getDsls().finalizeValue();
		getDsls().get().forEach(dsl -> {
			final SampleArchiveArtifact archive = project.getObjects().newInstance(SampleArchiveArtifact.class, names.artifactName(dsl.getNameAsCamelCase()), project);
			archive.getDsl().value(dsl).disallowChanges();
			artifacts.add(archive);
		});
	}
}
