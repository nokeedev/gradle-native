package dev.nokee.platform.ios.internal;

import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultSwiftIosApplicationExtension extends BaseNativeApplicationComponent implements SwiftIosApplicationExtension {
	@Inject
	public DefaultSwiftIosApplicationExtension(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}
}
