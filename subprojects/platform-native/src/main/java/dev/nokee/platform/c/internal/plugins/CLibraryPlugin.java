package dev.nokee.platform.c.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.c.CLibraryExtension;
import dev.nokee.platform.c.internal.DefaultCLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class CLibraryPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(CLibraryExtension.class, "library", getObjects().newInstance(DefaultCLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, NamingScheme.asMainComponent(project.getName()))));
	}
}
