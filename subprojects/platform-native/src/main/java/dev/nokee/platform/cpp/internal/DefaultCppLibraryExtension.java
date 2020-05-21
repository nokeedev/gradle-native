package dev.nokee.platform.cpp.internal;

import dev.nokee.platform.cpp.CppLibraryExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;

import javax.inject.Inject;

public abstract class DefaultCppLibraryExtension extends BaseNativeLibraryComponent implements CppLibraryExtension {
	@Inject
	public DefaultCppLibraryExtension(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}
}
