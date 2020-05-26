package dev.nokee.platform.c.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.c.internal.UTTypeCSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.c.CApplicationExtension;
import dev.nokee.runtime.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.runtime.nativebase.internal.DefaultNativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultCApplicationExtension extends BaseNativeApplicationComponent implements CApplicationExtension {
	@Inject
	public DefaultCApplicationExtension(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeCSource()).srcDir("src/main/c"));
	}
}
