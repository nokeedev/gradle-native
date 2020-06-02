package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import org.gradle.api.Action;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryVariant extends BaseNativeVariant implements NativeLibrary {
	private final DefaultNativeLibraryDependencies dependencies;

	@Inject
	public DefaultNativeLibraryVariant(String name, NamingScheme names, BuildVariant buildVariant, DefaultNativeLibraryDependencies dependencies) {
		super(name, names, buildVariant);
		this.dependencies = dependencies;
	}

	@Override
	public NativeLibraryDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeLibraryDependencies> action) {
		action.execute(dependencies);
	}
}
