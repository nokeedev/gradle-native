package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;

import javax.inject.Inject;

public abstract class DefaultNativeApplicationVariant extends BaseNativeVariant implements NativeApplication {
	@Getter private final DefaultNativeApplicationComponentDependencies dependencies;

	@Inject
	public DefaultNativeApplicationVariant(String name, NamingScheme names, BuildVariant buildVariant, VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> dependencies) {
		super(name, names, buildVariant);
		this.dependencies = dependencies.getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeApplicationComponentDependencies> action) {
		action.execute(dependencies);
	}
}
