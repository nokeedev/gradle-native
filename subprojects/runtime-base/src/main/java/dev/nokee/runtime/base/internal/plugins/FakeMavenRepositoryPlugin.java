/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.runtime.base.internal.plugins;

import dev.nokee.gradle.AdhocArtifactRepository;
import dev.nokee.gradle.AdhocArtifactRepositoryFactory;
import dev.nokee.runtime.base.internal.tools.ToolHandler;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class FakeMavenRepositoryPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val toolRepository = new ToolRepository();
		project.getExtensions().add("__nokee_toolRepository", toolRepository);
		project.getRepositories().add(createToolRepository(project));
	}

	private static AdhocArtifactRepository createToolRepository(Project project) {
		val repository = AdhocArtifactRepositoryFactory.forProject(project).create();
		repository.getCacheDirectory().set(project.getLayout().getBuildDirectory().dir("m2/tools"));

		val handler = new ToolHandler(project.getExtensions().getByType(ToolRepository.class));
		repository.setComponentSupplier(handler);
		repository.setComponentVersionLister(handler);
		repository.content(it -> it.includeGroup("dev.nokee.tool"));
		return repository;
	}
}
