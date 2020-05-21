package dev.nokee.platform.cpp.internal.plugins;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.cpp.CppLibraryExtension;
import dev.nokee.platform.cpp.internal.DefaultCppLibraryExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class CppLibraryPlugin implements Plugin<Project> {

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {

		project.getExtensions().add(CppLibraryExtension.class, "library", getObjects().newInstance(DefaultCppLibraryExtension.class,
			getObjects().newInstance(DefaultNativeLibraryDependencies.class, NamingScheme.asMainComponent(project.getName()))));
	}
}
