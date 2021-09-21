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
package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.visualstudio.VisualStudioIdeGuid;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import dev.nokee.ide.visualstudio.internal.tasks.GenerateVisualStudioIdeProjectTask;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DefaultVisualStudioIdeProject implements VisualStudioIdeProject, IdeProjectInternal {
	@Getter private final String name;
	@Getter private final TaskProvider<GenerateVisualStudioIdeProjectTask> generatorTask;

	@Inject
	public DefaultVisualStudioIdeProject(String name) {
		this.name = name;
		generatorTask = getTasks().register(name + "VisualStudioProject", GenerateVisualStudioIdeProjectTask.class, this);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateVisualStudioIdeProjectTask::getProjectLocation);
	}

	@Override
	public Provider<FileSystemLocation> getProjectLocation() {
		return getLocation();
	}

	public abstract ConfigurableFileCollection getSourceFiles();

	public abstract ConfigurableFileCollection getHeaderFiles();

	public abstract ConfigurableFileCollection getBuildFiles();

	public Provider<VisualStudioIdeGuid> getProjectGuid() {
		return getLocation().map(it -> DefaultVisualStudioIdeGuid.stableGuidFrom(it.getAsFile()));
	}

	public abstract DomainObjectSet<VisualStudioIdeTarget> getTargets();

	@Override
	public void target(VisualStudioIdeProjectConfiguration projectConfiguration, Action<? super VisualStudioIdeTarget> action) {
		val target = getTargets().stream().filter(it -> it.getProjectConfiguration().equals(projectConfiguration)).findAny().orElseGet(() -> newTarget(projectConfiguration));
		action.execute(target);
	}

	private DefaultVisualStudioIdeTarget newTarget(VisualStudioIdeProjectConfiguration projectConfiguration) {
		val result = new DefaultVisualStudioIdeTarget(projectConfiguration, getObjects());
		getTargets().add(result);
		return result;
	}

	@Override
	public String getDisplayName() {
		return "Visual Studio project";
	}

	@Override
	public Provider<Set<VisualStudioIdeProjectConfiguration>> getProjectConfigurations() {
		return getProviders().provider(() -> getTargets().stream().map(VisualStudioIdeTarget::getProjectConfiguration).collect(Collectors.toCollection(LinkedHashSet::new)));
	}
}
