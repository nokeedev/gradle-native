package dev.nokee.platform.nativebase.internal.plugins;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import dev.nokee.internal.Factory;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateSet;
import dev.nokee.runtime.core.Coordinates;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.RuntimeNativePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.not;
import static dev.nokee.runtime.nativebase.internal.DefaultBinaryLinkage.*;
import static dev.nokee.runtime.core.Coordinates.coordinateTypeOf;
import static dev.nokee.utils.TransformerUtils.collect;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;
import static java.util.stream.Collectors.joining;

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(RuntimeNativePlugin.class);
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
			// Handle linkage dimension
			if (component instanceof TargetLinkageAwareComponent) {
				projection.getDimensions().add(((TargetLinkageAwareComponent) component).getTargetLinkages()
					.map(assertNonEmpty("target linkage", projection.getIdentifier().getName().toString()))
					.map(assertSupportedValues(SHARED, STATIC))
					.map(toSetTransformer(coordinateTypeOf(TargetLinkage.class)).andThen(collect(Coordinates.toCoordinateSet()))));
			} else if (projection instanceof DefaultNativeApplicationComponent) {
				projection.getDimensions().add(CoordinateSet.of(EXECUTABLE));
			} else if (projection instanceof DefaultNativeLibraryComponent) {
				projection.getDimensions().add(CoordinateSet.of(SHARED));
			}

			// Handle build type dimension
			if (component instanceof TargetBuildTypeAwareComponent) {
				projection.getDimensions().add(((TargetBuildTypeAwareComponent) component).getTargetBuildTypes()
					.map(assertNonEmpty("target build type", projection.getIdentifier().getName().toString()))
					.map(toSetTransformer(coordinateTypeOf(TargetBuildType.class)).andThen(collect(Coordinates.toCoordinateSet()))));
			} else {
				projection.getDimensions().add(CoordinateSet.of(Coordinates.of(TargetBuildTypes.DEFAULT)));
			}

			// Handle operating system family and machine architecture dimension
			if (component instanceof TargetMachineAwareComponent) {
				projection.getDimensions().add(((TargetMachineAwareComponent) component).getTargetMachines()
					.map(assertNonEmpty("target machine", projection.getIdentifier().getName().toString()))
					.map(toSetTransformer(coordinateTypeOf(TargetMachine.class)).andThen(collect(Coordinates.toCoordinateSet()))));
			}

			projection.getBuildVariants().convention(projection.getFinalSpace().map(DefaultBuildVariant::fromSpace));
			projection.getBuildVariants().finalizeValueOnRead();
			projection.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
		};
	}

	private static <I extends Iterable<T>, T> Transformer<I, I> assertNonEmpty(String propertyName, String componentName) {
		return values -> {
			if (Iterables.isEmpty(values)) {
				throw new IllegalArgumentException(String.format("A %s needs to be specified for component '%s'.", propertyName, componentName));
			}
			return values;
		};
	}

	private static <I extends Iterable<T>, T> Transformer<I, I> assertSupportedValues(T... supportedValues) {
		return assertSupportedValues(ImmutableSet.copyOf(supportedValues));
	}

	private static <I extends Iterable<T>, T> Transformer<I, I> assertSupportedValues(Set<T> supportedValues) {
		return values -> {
			val unsupportedValues = Streams.stream(values).filter(not(supportedValues::contains)).collect(Collectors.toList());
			if (!unsupportedValues.isEmpty()) {
				throw new IllegalArgumentException("The following values are not supported:\n" + unsupportedValues.stream().map(it -> " * " + it).collect(joining("\n")));
			}
			return values;
		};
	}

	public static Action<Project> finalizeModelNodeOf(Object target) {
		return project -> ModelNodes.of(target).finalizeValue();
	}
}
