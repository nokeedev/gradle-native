package dev.nokee.platform.ios.internal;

import dev.nokee.platform.ios.ObjectiveCIosLibraryExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;

import javax.inject.Inject;

public abstract class DefaultObjectiveCIosLibraryExtension extends BaseNativeLibraryComponent implements ObjectiveCIosLibraryExtension {
	@Inject
	public DefaultObjectiveCIosLibraryExtension(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}
}
