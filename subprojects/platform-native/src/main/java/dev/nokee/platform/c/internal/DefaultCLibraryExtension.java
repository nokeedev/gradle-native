package dev.nokee.platform.c.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.c.internal.UTTypeCSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.c.CLibraryExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;

import javax.inject.Inject;

public abstract class DefaultCLibraryExtension extends BaseNativeLibraryComponent implements CLibraryExtension {
	@Inject
	public DefaultCLibraryExtension(DefaultNativeLibraryDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeCSource()).srcDir("src/main/c"));
	}
}
