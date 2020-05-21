package dev.nokee.platform.ios.internal;

import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultObjectiveCIosApplicationExtension extends BaseNativeApplicationComponent implements ObjectiveCIosApplicationExtension {
	@Inject
	public DefaultObjectiveCIosApplicationExtension(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}
}
