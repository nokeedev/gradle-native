package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;
import org.gradle.api.NamedDomainObjectContainer;

public interface IdeProjectExtension<T extends IdeProject> {
	NamedDomainObjectContainer<T> getProjects();
}
