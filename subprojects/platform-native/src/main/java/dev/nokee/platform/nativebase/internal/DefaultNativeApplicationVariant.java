package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.BinaryAwareNativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;

import javax.inject.Inject;

public abstract class DefaultNativeApplicationVariant extends BaseNativeVariant implements NativeApplication {
	@Getter private final BinaryAwareNativeComponentDependencies dependencies;

	@Inject
	public DefaultNativeApplicationVariant(String name, NamingScheme names, BuildVariant buildVariant, BinaryAwareNativeComponentDependencies dependencies) {
		super(name, names, buildVariant);
		this.dependencies = dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(dependencies);
	}
}
