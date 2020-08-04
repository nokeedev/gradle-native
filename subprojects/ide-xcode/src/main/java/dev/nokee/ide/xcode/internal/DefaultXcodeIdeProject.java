package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.xcode.XcodeIdeGroup;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.tasks.GenerateXcodeIdeProjectTask;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeProject implements XcodeIdeProject, IdeProjectInternal {
	@Getter(onMethod_={@Internal}) private final String name;
	@Getter(onMethod_={@Internal}) private final TaskProvider<GenerateXcodeIdeProjectTask> generatorTask;
	@Getter(onMethod_={@Internal}) private final NamedDomainObjectContainer<XcodeIdeTarget> targets;
	@Getter(onMethod_={@Internal}) private final NamedDomainObjectContainer<XcodeIdeGroup> groups;

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

	@InputDirectory
	@Override
	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateXcodeIdeProjectTask::getProjectLocation);
	}

	@Override
	public void targets(Action<? super NamedDomainObjectContainer<XcodeIdeTarget>> action) {
		action.execute(targets);
	}

	private XcodeIdeTarget newTarget(String name) {
		return getObjects().newInstance(DefaultXcodeIdeTarget.class, name);
	}

	public void groups(Action<? super NamedDomainObjectContainer<XcodeIdeGroup>> action) {
		action.execute(getGroups());
	}

	private XcodeIdeGroup newGroup(String name) {
		return getObjects().newInstance(DefaultXcodeIdeGroup.class, name);
	}

	@Internal
	@Override
	public XcodeIdeProject getIdeProject() {
		return this;
	}

	@Internal
	@Override
	public String getDisplayName() {
		return "Xcode project";
	}

	@Internal
	public abstract ConfigurableFileCollection getSources();
}
