package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectExtension;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public abstract class DefaultVisualStudioIdeProjectExtension implements VisualStudioIdeProjectExtension, IdeProjectExtension<VisualStudioIdeProject>, HasPublicType {
	@Getter private final NamedDomainObjectContainer<VisualStudioIdeProject> projects;

	@Inject
	public DefaultVisualStudioIdeProjectExtension() {
		projects = getObjects().domainObjectContainer(VisualStudioIdeProject.class, this::newProject);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void projects(Action<? super NamedDomainObjectContainer<VisualStudioIdeProject>> action) {
		action.execute(projects);
	}

	private VisualStudioIdeProject newProject(String name) {
		return getObjects().newInstance(DefaultVisualStudioIdeProject.class, name);
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(VisualStudioIdeProjectExtension.class);
	}
}
