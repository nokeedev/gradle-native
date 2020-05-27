package dev.nokee.platform.objectivec.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.objectivec.internal.UTTypeObjectiveCSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCApplicationExtension extends BaseNativeApplicationComponent implements ObjectiveCApplicationExtension {
	@Inject
	public DefaultObjectiveCApplicationExtension(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCSource()).srcDir("src/main/objc"));
	}
}
