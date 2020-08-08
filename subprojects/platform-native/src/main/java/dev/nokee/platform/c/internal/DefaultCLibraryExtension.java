package dev.nokee.platform.c.internal;

import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.c.CLibraryExtension;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultCLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements CLibraryExtension {
	@Inject
	public DefaultCLibraryExtension(DefaultNativeLibraryComponent component) {
		super(component);
		getComponent().getSourceCollection().add(getObjects().newInstance(CSourceSet.class, "c").from(getSources().getElements().map(toIfEmpty("src/main/c"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "headers").srcDir(getPrivateHeaders().getElements().map(toIfEmpty("src/main/headers"))));
		getComponent().getSourceCollection().add(getObjects().newInstance(CHeaderSet.class, "public").srcDir(getPublicHeaders().getElements().map(toIfEmpty("src/main/public"))));
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
