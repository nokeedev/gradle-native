package dev.nokee.platform.cpp.internal;

import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.cpp.CppApplicationExtension;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultCppApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements CppApplicationExtension {
	@Inject
	public DefaultCppApplicationExtension(DefaultNativeApplicationComponent component) {
		super(component);
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

	@Override
	public VariantView<NativeApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeApplication.class);
	}
}
