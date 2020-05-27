package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.objectivecpp.internal.UTTypeObjectiveCppSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCppLibraryExtension extends BaseNativeLibraryComponent implements ObjectiveCppLibraryExtension {
	@Inject
	public DefaultObjectiveCppLibraryExtension(DefaultNativeLibraryDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCppSource()).srcDir("src/main/objcpp"));
	}
}
