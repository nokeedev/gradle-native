package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.base.internal.UTTypeSourceCode;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.Cast;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseNativeComponent {
	private final DefaultNativeComponentDependencies dependencies;
	private final NamingScheme names;
	private final DomainObjectSet<SourceSet<UTTypeSourceCode>> sourceCollection;

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();

	public BaseNativeComponent(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		this.dependencies = dependencies;
		this.names = names;
		this.sourceCollection = Cast.uncheckedCast(getObjects().domainObjectSet(SourceSet.class));

		getDimensions().convention(getProviders().provider(this::createDimensions));
		getDimensions().finalizeValueOnRead();
		getDimensions().disallowChanges(); // Let's disallow changing them for now.

		getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		getBuildVariants().finalizeValueOnRead();
		getBuildVariants().disallowChanges(); // Let's disallow changing them for now.
	}

	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	public DomainObjectSet<SourceSet<UTTypeSourceCode>> getSourceCollection() {
		return sourceCollection;
	}

	// TODO: Should be part of the extension not the component
	// Not every implementation needs this, so we rely on the public type to expose it.
	public DefaultTargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}

	// TODO: Should use a strategy pattern or some sort of mixed in
	protected Iterable<BuildVariant> createBuildVariants() {
		if (this instanceof TargetMachineAwareComponent) {
			Set<TargetMachine> targetMachines = ((TargetMachineAwareComponent) this).getTargetMachines().get();
			return targetMachines.stream().map(it -> (DefaultTargetMachine)it).map(it -> DefaultBuildVariant.of(it.getOperatingSystemFamily(), it.getArchitecture())).collect(Collectors.toList());
		}
		throw new GradleException("Not able to create the default build variants");
	}

	// TODO: Should be part of the component configuration
	protected abstract Iterable<DimensionType> createDimensions();

	// TODO: We may want to model this as a DimensionRegistry for more richness than a plain set
	public abstract SetProperty<DimensionType> getDimensions();

	// TODO: We may want to model this as a BuildVariantRegistry for more richness than a plain set
	public abstract SetProperty<BuildVariant> getBuildVariants();

	public void finalizeExtension(Project project) {
		getBuildVariants().get().forEach(buildVariant -> {
			final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
			final NamingScheme names = this.names.forBuildVariant(buildVariant, getBuildVariants().get());

			getTasks().register(names.getTaskName("objects"), task -> {
				task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
				task.setDescription("Assembles main objects.");
			});

			if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
				DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
				if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
					getTasks().register(names.getTaskName("sharedLibrary"), task -> {
						task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
						task.setDescription("Assembles a shared library binary containing the main objects.");
					});
				} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
					getTasks().register(names.getTaskName("staticLibrary"), task -> {
						task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
						task.setDescription("Assembles a static library binary containing the main objects.");
					});
				} else if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
					getTasks().register(names.getTaskName("executable"), task -> {
						task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
						task.setDescription("Assembles a executable binary containing the main objects.");
					});
				}
			}

			if (getBuildVariants().get().size() > 1) {
				getTasks().register(names.getTaskName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
				});
			}
		});

		// finalize the variantCollection
	}
}
