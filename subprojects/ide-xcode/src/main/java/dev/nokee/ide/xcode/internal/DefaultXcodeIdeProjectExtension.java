package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeProjectExtension implements XcodeIdeProjectExtension {
	private static final XcodeIdeProductTypes PRODUCT_TYPES_FACTORY = new XcodeIdeProductTypes() {};
	@Getter private final NamedDomainObjectContainer<XcodeIdeProject> projects;

	@Inject
	public DefaultXcodeIdeProjectExtension() {
		projects = getObjects().domainObjectContainer(XcodeIdeProject.class, this::newProject);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void projects(Action<? super NamedDomainObjectContainer<XcodeIdeProject>> action) {
		action.execute(projects);
	}

	private XcodeIdeProject newProject(String name) {
		return getObjects().newInstance(DefaultXcodeIdeProject.class, name);
	}

	@Override
	public XcodeIdeProductTypes getProductTypes() {
		return PRODUCT_TYPES_FACTORY;
	}
}
