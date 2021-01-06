package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.BuildVariantsCallable;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.function.BiConsumer;

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
	}

	public static Factory<DefaultNativeApplicationComponent> nativeApplicationProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultNativeApplicationComponent.class, ProjectIdentifier.of(project));
		return () -> new DefaultNativeApplicationComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(ModelLookup.class));
	}

	public static Factory<DefaultNativeLibraryComponent> nativeLibraryProjection(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultNativeLibraryComponent.class, ProjectIdentifier.of(project));
		return () -> new DefaultNativeLibraryComponent(identifier, project.getObjects(), project.getProviders(), project.getTasks(), project.getLayout(), project.getConfigurations(), project.getDependencies(), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(VariantViewFactory.class), project.getExtensions().getByType(VariantRepository.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(TaskViewFactory.class), project.getExtensions().getByType(ModelLookup.class));
	}

	public static <T extends Component, PROJECTION> Action<T> configureUsingProjection(Class<PROJECTION> type, BiConsumer<? super T, ? super PROJECTION> action) {
		return t -> action.accept(t, ModelNodes.of(t).get(type));
	}

	public static <T extends Component, PROJECTION extends BaseComponent<?>> BiConsumer<T, PROJECTION> baseNameConvention(String baseName) {
		return (t, projection) -> projection.getBaseName().convention(baseName);
	}

	public static <T extends Component, PROJECTION extends BaseComponent<?>> BiConsumer<T, PROJECTION> configureBuildVariants() {
		return (component, projection) -> {
			projection.getBuildVariants().convention(ProviderUtils.supplied(new BuildVariantsCallable(component, projection, projection.getDimensions()::get)));
			projection.getBuildVariants().finalizeValueOnRead();
			projection.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

			projection.getDimensions().disallowChanges(); // Let's disallow changing them for now.
		};
	}

	public static Action<Project> finalizeModelNodeOf(Object target) {
		return project -> ModelNodes.of(target).finalizeValue();
	}
}
