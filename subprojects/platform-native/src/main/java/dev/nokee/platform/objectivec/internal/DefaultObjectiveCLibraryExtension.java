package dev.nokee.platform.objectivec.internal;

import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCLibraryExtension extends BaseNativeLibraryComponent implements ObjectiveCLibraryExtension {
	@Inject
	public DefaultObjectiveCLibraryExtension(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}
}
