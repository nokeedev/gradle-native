package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;
import org.gradle.api.NamedDomainObjectContainer;

public interface IdeProjectExtension {
	NamedDomainObjectContainer<? extends IdeProject> getProjects();
}
