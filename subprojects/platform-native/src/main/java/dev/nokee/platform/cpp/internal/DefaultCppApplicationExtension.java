package dev.nokee.platform.cpp.internal;

import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.cpp.CppApplicationExtension;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultCppApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements CppApplicationExtension {
	@Inject
	public DefaultCppApplicationExtension(NamingScheme names) {
		super(names, DefaultNativeApplicationComponent.class);
		getComponent().getSourceCollection().add(getObjects().newInstance(CppSourceSet.class).srcDir("src/main/cpp"));
	}

	@Override
	public NativeComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		getComponent().dependencies(action);
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}
}
