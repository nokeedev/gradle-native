package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCppLibraryExtension extends BaseNativeLibraryComponent implements ObjectiveCppLibraryExtension {
	@Inject
	public DefaultObjectiveCppLibraryExtension(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}
}
