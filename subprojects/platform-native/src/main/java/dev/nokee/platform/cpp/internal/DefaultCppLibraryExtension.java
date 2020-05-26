package dev.nokee.platform.cpp.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.cpp.internal.UTTypeCppSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.cpp.CppLibraryExtension;
import dev.nokee.runtime.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.runtime.nativebase.internal.DefaultNativeLibraryDependencies;

import javax.inject.Inject;

public abstract class DefaultCppLibraryExtension extends BaseNativeLibraryComponent implements CppLibraryExtension {
	@Inject
	public DefaultCppLibraryExtension(DefaultNativeLibraryDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeCppSource()).srcDir("src/main/cpp"));
	}
}
