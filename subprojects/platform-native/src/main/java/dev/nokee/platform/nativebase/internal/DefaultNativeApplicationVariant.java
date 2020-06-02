package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeApplication;

import javax.inject.Inject;

public abstract class DefaultNativeApplicationVariant extends BaseNativeVariant implements NativeApplication {
	@Inject
	public DefaultNativeApplicationVariant(String name, NamingScheme names, BuildVariant buildVariant, DefaultNativeComponentDependencies dependencies) {
		super(name, names, buildVariant);
	}
}
