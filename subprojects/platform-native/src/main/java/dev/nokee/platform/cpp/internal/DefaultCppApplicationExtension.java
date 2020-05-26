package dev.nokee.platform.cpp.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.cpp.internal.UTTypeCppSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.cpp.CppApplicationExtension;
import dev.nokee.runtime.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.runtime.nativebase.internal.DefaultNativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultCppApplicationExtension extends BaseNativeApplicationComponent implements CppApplicationExtension {
	@Inject
	public DefaultCppApplicationExtension(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
		getSourceCollection().add(getObjects().newInstance(DefaultSourceSet.class, new UTTypeCppSource()).srcDir("src/main/cpp"));
	}
}
