package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.objectivecpp.internal.UTTypeObjectiveCppSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.runtime.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.runtime.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCppApplicationExtension extends BaseNativeApplicationComponent implements ObjectiveCppApplicationExtension {
	@Inject
	public DefaultObjectiveCppApplicationExtension(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCppSource()).srcDir("src/main/objcpp"));
	}
}
