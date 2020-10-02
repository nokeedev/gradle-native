package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public class DefaultNativeLibraryVariant extends BaseNativeVariant implements NativeLibrary, VariantInternal {
	@Getter private final DefaultNativeLibraryComponentDependencies dependencies;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;
	@Getter private final ResolvableComponentDependencies resolvableDependencies;

	@Inject
	public DefaultNativeLibraryVariant(VariantIdentifier<DefaultNativeLibraryVariant> identifier, NamingScheme names, VariantComponentDependencies<DefaultNativeLibraryComponentDependencies> dependencies, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers, ProjectLayout layout, TaskProvider<Task> assembleTask) {
		super(identifier, names, objects, tasks, providers, assembleTask);
		this.dependencies = dependencies.getDependencies();
		this.layout = layout;
		this.resolvableDependencies = dependencies.getIncoming();
	}
}
