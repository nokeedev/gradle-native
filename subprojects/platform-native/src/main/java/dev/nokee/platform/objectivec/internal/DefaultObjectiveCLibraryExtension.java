package dev.nokee.platform.objectivec.internal;

import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultObjectiveCLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements ObjectiveCLibraryExtension {
	@Inject
	public DefaultObjectiveCLibraryExtension(DefaultNativeLibraryComponent component) {
		super(component);
		getComponent().getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class, "objc").from(getSources().getElements().map(toIfEmpty("src/main/objc"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir(getPrivateHeaders().getElements().map(toIfEmpty("src/main/headers"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "public").srcDir(getPublicHeaders().getElements().map(toIfEmpty("src/main/public"))));
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

	@Override
	public VariantView<NativeLibrary> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeLibrary.class);
	}
}
