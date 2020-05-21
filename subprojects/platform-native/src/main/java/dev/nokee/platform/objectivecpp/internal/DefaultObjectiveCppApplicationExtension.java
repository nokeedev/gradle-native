package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationExtension;

import javax.inject.Inject;

public abstract class DefaultObjectiveCppApplicationExtension extends BaseNativeApplicationComponent implements ObjectiveCppApplicationExtension {
	@Inject
	public DefaultObjectiveCppApplicationExtension(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}
}
