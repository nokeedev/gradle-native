package dev.nokee.platform.c.internal;

import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.c.CLibraryExtension;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultCLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements CLibraryExtension {
	@Inject
	public DefaultCLibraryExtension(DefaultNativeLibraryComponent component) {
		super(component);
		getComponent().getSourceCollection().add(getObjects().newInstance(CSourceSet.class).srcDir("src/main/c"));
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
