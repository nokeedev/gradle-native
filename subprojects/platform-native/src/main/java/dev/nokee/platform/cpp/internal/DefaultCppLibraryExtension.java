package dev.nokee.platform.cpp.internal;

import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.cpp.CppLibraryExtension;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultCppLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements CppLibraryExtension {
	@Inject
	public DefaultCppLibraryExtension(NamingScheme names) {
		super(names, DefaultNativeLibraryComponent.class);
		getComponent().getSourceCollection().add(getObjects().newInstance(CppSourceSet.class).srcDir("src/main/cpp"));
	}

	@Override
	public NativeLibraryDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeLibraryDependencies> action) {
		getComponent().dependencies(action);
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}
}
