package dev.nokee.platform.c.internal;

import dev.nokee.platform.c.CApplicationExtension;
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultCApplicationExtension extends BaseNativeApplicationComponent implements CApplicationExtension {
	@Inject
	public DefaultCApplicationExtension(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}
}
