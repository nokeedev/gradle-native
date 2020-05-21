package dev.nokee.platform.c.internal;

import dev.nokee.platform.c.CLibraryExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;

import javax.inject.Inject;

public abstract class DefaultCLibraryExtension extends BaseNativeLibraryComponent implements CLibraryExtension {
	@Inject
	public DefaultCLibraryExtension(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}
}
