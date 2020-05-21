package dev.nokee.platform.ios.internal;

import dev.nokee.platform.ios.SwiftIosLibraryExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;

import javax.inject.Inject;

public abstract class DefaultSwiftIosLibraryExtension extends BaseNativeLibraryComponent implements SwiftIosLibraryExtension {
	@Inject
	public DefaultSwiftIosLibraryExtension(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}
}
