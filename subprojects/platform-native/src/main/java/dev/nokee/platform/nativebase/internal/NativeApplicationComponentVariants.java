package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.TaskProvider;

import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public final class NativeApplicationComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<DefaultNativeApplicationVariant> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	@Getter private final Provider<DefaultNativeApplicationVariant> developmentVariant;
	private final ObjectFactory objectFactory;
	private final DefaultNativeApplicationComponent component;
	private final DependencyHandler dependencyHandler;
	private final ConfigurationContainer configurationContainer;
	private final ProviderFactory providerFactory;
	private final TaskRegistry taskRegistry;

	public NativeApplicationComponentVariants(ObjectFactory objectFactory, DefaultNativeApplicationComponent component, DependencyHandler dependencyHandler, ConfigurationContainer configurationContainer, ProviderFactory providerFactory, TaskRegistry taskRegistry, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository) {
		this.variantCollection = new VariantCollection<>(component.getIdentifier(), DefaultNativeApplicationVariant.class, eventPublisher, viewFactory, variantRepository);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.developmentVariant = providerFactory.provider(new BuildableDevelopmentVariantConvention<>(() -> getVariantCollection().get()));
		this.objectFactory = objectFactory;
		this.component = component;
		this.dependencyHandler = dependencyHandler;
		this.configurationContainer = configurationContainer;
		this.providerFactory = providerFactory;
		this.taskRegistry = taskRegistry;
	}

	public void calculateVariants() {
		getBuildVariants().get().forEach(buildVariant -> {
			val variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeApplicationVariant.class).build();

			val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), variantIdentifier));

			val dependencies = newDependencies(buildVariant, variantIdentifier);
			val variant = getVariantCollection().registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies, assembleTask));

			onEachVariantDependencies(variant, dependencies);
		});
	}

	private VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> newDependencies(BuildVariantInternal buildVariant, VariantIdentifier<DefaultNativeApplicationVariant> variantIdentifier) {
		var variantDependencies = component.getDependencies();
		if (getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		boolean hasSwift = !component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(variantDependencies).withVariant(buildVariant);
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		val incoming = incomingDependenciesBuilder.buildUsing(objectFactory);
		NativeOutgoingDependencies outgoing = objectFactory.newInstance(NativeApplicationOutgoingDependencies.class, variantIdentifier, buildVariant, variantDependencies);

		return new VariantComponentDependencies<>(variantDependencies, incoming, outgoing);
	}

	private DefaultNativeApplicationVariant createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> variantDependencies, TaskProvider<Task> assembleTask) {
		return new DefaultNativeApplicationVariant(identifier, variantDependencies, objectFactory, providerFactory, assembleTask);
	}

	private void onEachVariantDependencies(VariantProvider<DefaultNativeApplicationVariant> variant, VariantComponentDependencies<?> dependencies) {
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}
}
