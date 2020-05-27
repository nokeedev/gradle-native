package dev.nokee.platform.swift.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.swift.internal.UTTypeSwiftSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.swift.SwiftLibraryExtension;

import javax.inject.Inject;

public abstract class DefaultSwiftLibraryExtension extends BaseNativeLibraryComponent implements SwiftLibraryExtension {
	@Inject
	public DefaultSwiftLibraryExtension(DefaultNativeLibraryDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeSwiftSource()).srcDir("src/main/swift"));
	}
}
