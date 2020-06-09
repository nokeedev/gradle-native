package dev.nokee.platform.swift.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.swift.SwiftLibraryExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultSwiftLibraryExtension extends BaseNativeExtension<DefaultNativeLibraryComponent> implements SwiftLibraryExtension {
	@Inject
	public DefaultSwiftLibraryExtension(DefaultNativeLibraryComponent component) {
		super(component);
		getComponent().getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class).srcDir("src/main/swift"));
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
