package dev.nokee.platform.swift.internal;

import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.swift.SwiftApplicationExtension;

import javax.inject.Inject;

public abstract class DefaultSwiftApplicationExtension extends BaseNativeApplicationComponent implements SwiftApplicationExtension {
	@Inject
	public DefaultSwiftApplicationExtension(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}
}
