package dev.nokee.platform.swift.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.swift.internal.UTTypeSwiftSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.runtime.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.runtime.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.swift.SwiftApplicationExtension;

import javax.inject.Inject;

public abstract class DefaultSwiftApplicationExtension extends BaseNativeApplicationComponent implements SwiftApplicationExtension {
	@Inject
	public DefaultSwiftApplicationExtension(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeSwiftSource()).srcDir("src/main/swift"));
	}
}
