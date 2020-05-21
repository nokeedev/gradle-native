package dev.nokee.platform.objectivec.internal;

import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCApplicationExtension extends BaseNativeApplicationComponent implements ObjectiveCApplicationExtension {
	@Inject
	public DefaultObjectiveCApplicationExtension(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}
}
