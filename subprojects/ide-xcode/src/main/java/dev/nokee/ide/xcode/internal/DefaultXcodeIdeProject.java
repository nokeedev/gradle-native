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
package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.xcode.XcodeIdeGroup;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.tasks.GenerateXcodeIdeProjectTask;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeProject implements XcodeIdeProject, IdeProjectInternal {
	@Getter private final String name;
	@Getter private final TaskProvider<GenerateXcodeIdeProjectTask> generatorTask;
	@Getter private final NamedDomainObjectContainer<XcodeIdeTarget> targets;
	@Getter private final NamedDomainObjectContainer<XcodeIdeGroup> groups;

	@Inject
	public DefaultXcodeIdeProject(String name) {
		this.name = name;
		this.targets = getObjects().domainObjectContainer(XcodeIdeTarget.class, this::newTarget);
		this.groups = getObjects().domainObjectContainer(XcodeIdeGroup.class, this::newGroup);
		generatorTask = getTasks().register(name + "XcodeProject", GenerateXcodeIdeProjectTask.class, this);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateXcodeIdeProjectTask::getProjectLocation);
	}

	@Override
	public void targets(Action<? super NamedDomainObjectContainer<XcodeIdeTarget>> action) {
		action.execute(targets);
	}

	private XcodeIdeTarget newTarget(String name) {
		return new DefaultXcodeIdeTarget(name, getObjects());
	}

	public void groups(Action<? super NamedDomainObjectContainer<XcodeIdeGroup>> action) {
		action.execute(getGroups());
	}

	private XcodeIdeGroup newGroup(String name) {
		return new DefaultXcodeIdeGroup(name, getObjects());
	}

	@Override
	public XcodeIdeProject getIdeProject() {
		return this;
	}

	@Override
	public String getDisplayName() {
		return "Xcode project";
	}
}
