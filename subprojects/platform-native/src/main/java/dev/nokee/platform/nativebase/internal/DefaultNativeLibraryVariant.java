package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibrary;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryVariant extends BaseNativeVariant implements NativeLibrary {
	@Inject
	public DefaultNativeLibraryVariant(String name, NamingScheme names, BuildVariant buildVariant, DefaultNativeLibraryDependencies dependencies) {
		super(name, names, buildVariant);
	}
}
