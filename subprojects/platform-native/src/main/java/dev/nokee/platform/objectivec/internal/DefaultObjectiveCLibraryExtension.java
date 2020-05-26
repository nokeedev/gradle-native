package dev.nokee.platform.objectivec.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.objectivec.internal.UTTypeObjectiveCSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.runtime.nativebase.internal.BaseNativeLibraryComponent;
import dev.nokee.runtime.nativebase.internal.DefaultNativeLibraryDependencies;
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCLibraryExtension extends BaseNativeLibraryComponent implements ObjectiveCLibraryExtension {
	@Inject
	public DefaultObjectiveCLibraryExtension(DefaultNativeLibraryDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCSource()).srcDir("src/main/objc"));
	}
}
