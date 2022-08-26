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
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class DependsOnAllGradleVersionSourceSet implements Action<SourceSet> {
	private final Supplier<Iterable<String>> versions;
	private final ConfigurationContainer configurations;
	private final DependencyHandler dependencies;
	private final Project thisProject;
	private final ObjectFactory objects;
	private final ProviderFactory providers;

	public DependsOnAllGradleVersionSourceSet(Project project) {
		this.configurations = project.getConfigurations();
		this.dependencies = project.getDependencies();
		this.versions = () -> project.getExtensions().getByType(SourceSetContainer.class).stream().map(SourceSet::getName).filter(it -> it.startsWith("v")).collect(Collectors.toList());
		this.thisProject = project;
		this.objects = project.getObjects();
		this.providers = project.getProviders();
	}

	@Override
	public void execute(SourceSet sourceSet) {
		DependencySet runtimeOnlyDependencies = configurations.getByName(sourceSet.getRuntimeOnlyConfigurationName()).getDependencies();
		runtimeOnlyDependencies.addAllLater(objects.setProperty(Dependency.class).value(providers.provider(() -> {
			return StreamSupport.stream(versions.get().spliterator(), false).map(this::createDependency).collect(Collectors.toList());
		})));
	}

	private Dependency createDependency(String version) {
		ModuleDependency result = (ModuleDependency) dependencies.create(thisProject);
		result.capabilities(it -> it.requireCapability("dev.nokee:" + thisProject.getName() + "-gradle-" + version));
		return result;
	}
}
