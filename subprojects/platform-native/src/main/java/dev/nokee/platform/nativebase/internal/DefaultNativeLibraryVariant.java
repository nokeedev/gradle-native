package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class DefaultNativeLibraryVariant extends BaseNativeVariant implements NativeLibrary {
	@Getter private final DefaultNativeLibraryComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;

	@Inject
	public DefaultNativeLibraryVariant(String name, NamingScheme names, BuildVariantInternal buildVariant, VariantComponentDependencies<DefaultNativeLibraryComponentDependencies> dependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers, ProjectLayout layout) {
		super(name, names, buildVariant, objects, tasks, providers);
		this.dependencies = dependencies.getDependencies();
		this.layout = layout;
	}

	@Override
	public void dependencies(Action<? super NativeLibraryComponentDependencies> action) {
		action.execute(dependencies);
	}
}
