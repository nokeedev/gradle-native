package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultObjectiveCppLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements ObjectiveCppLibraryExtension {
	@Inject
	public DefaultObjectiveCppLibraryExtension(DefaultNativeLibraryComponent component) {
		super(component);
		getComponent().getSourceCollection().add(getObjects().newInstance(ObjectiveCppSourceSet.class, "objcpp").from(getSources().getElements().map(toIfEmpty("src/main/objcpp"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CppHeaderSet.class, "headers").srcDir(getPrivateHeaders().getElements().map(toIfEmpty("src/main/headers"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CppHeaderSet.class, "public").srcDir(getPublicHeaders().getElements().map(toIfEmpty("src/main/public"))));
	}

	@Override
	public NativeLibraryComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeLibraryComponentDependencies> action) {
		getComponent().dependencies(action);
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<NativeLibrary> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeLibrary.class);
	}
}
