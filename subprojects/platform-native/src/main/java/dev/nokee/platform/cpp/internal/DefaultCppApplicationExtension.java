package dev.nokee.platform.cpp.internal;

import dev.nokee.platform.cpp.CppApplicationExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultCppApplicationExtension extends BaseNativeApplicationComponent implements CppApplicationExtension {
	@Inject
	public DefaultCppApplicationExtension(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}
}
