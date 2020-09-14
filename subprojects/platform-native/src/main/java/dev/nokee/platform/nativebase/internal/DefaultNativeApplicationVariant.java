package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeApplicationVariant extends BaseNativeVariant implements NativeApplication, VariantInternal {
	@Getter private final DefaultNativeApplicationComponentDependencies dependencies;

	@Inject
	public DefaultNativeApplicationVariant(String name, NamingScheme names, BuildVariantInternal buildVariant, VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> dependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers) {
		super(name, names, buildVariant, objects, tasks, providers);
		this.dependencies = dependencies.getDependencies();
	}
}
