package dev.nokee.platform.swift.internal;

import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.swift.SwiftLibraryExtension;

import javax.inject.Inject;

public abstract class DefaultSwiftLibraryExtension extends BaseNativeLibraryComponent implements SwiftLibraryExtension {
	@Inject
	public DefaultSwiftLibraryExtension(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}
}
