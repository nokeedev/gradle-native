package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.ProjectLayout;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryVariant extends BaseNativeVariant implements NativeLibrary {
	@Getter private final DefaultNativeLibraryComponentDependencies dependencies;

	@Inject
	public DefaultNativeLibraryVariant(String name, NamingScheme names, BuildVariant buildVariant, VariantComponentDependencies<DefaultNativeLibraryComponentDependencies> dependencies) {
		super(name, names, buildVariant);
		this.dependencies = dependencies.getDependencies();
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Override
	public void dependencies(Action<? super NativeLibraryComponentDependencies> action) {
		action.execute(dependencies);
	}
}
