package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.base.internal.LanguageSourceSetRepository;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import lombok.Getter;
import lombok.val;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.withType;

public final class JavaNativeInterfaceComponentVariants implements ComponentVariants {
	@Getter private final VariantCollection<JniLibraryInternal> variantCollection;
	@Getter private final SetProperty<BuildVariantInternal> buildVariants;
	@Getter private final Provider<JniLibraryInternal> developmentVariant;
	private final ObjectFactory objectFactory;
	private final JniLibraryComponentInternal component;
	private final ConfigurationContainer configurationContainer;
	private final DependencyHandler dependencyHandler;
	private final ProviderFactory providerFactory;
	private final TaskRegistry taskRegistry;
	private final DomainObjectEventPublisher eventPublisher;
	private final BinaryViewFactory binaryViewFactory;
	private final TaskViewFactory taskViewFactory;
	private final LanguageSourceSetRepository languageSourceSetRepository;

	public JavaNativeInterfaceComponentVariants(ObjectFactory objectFactory, JniLibraryComponentInternal component, ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler, ProviderFactory providerFactory, TaskRegistry taskRegistry, DomainObjectEventPublisher eventPublisher, VariantViewFactory viewFactory, VariantRepository variantRepository, BinaryViewFactory binaryViewFactory, TaskViewFactory taskViewFactory, LanguageSourceSetRepository languageSourceSetRepository) {
		this.eventPublisher = eventPublisher;
		this.binaryViewFactory = binaryViewFactory;
		this.taskViewFactory = taskViewFactory;
		this.languageSourceSetRepository = languageSourceSetRepository;
		this.variantCollection = new VariantCollection<>(component.getIdentifier(), JniLibraryInternal.class, eventPublisher, viewFactory, variantRepository);
		this.buildVariants = objectFactory.setProperty(BuildVariantInternal.class);
		this.developmentVariant = providerFactory.provider(new BuildableDevelopmentVariantConvention<>(getVariantCollection()::get));
		this.objectFactory = objectFactory;
		this.component = component;
		this.configurationContainer = configurationContainer;
		this.dependencyHandler = dependencyHandler;
		this.providerFactory = providerFactory;
		this.taskRegistry = taskRegistry;
	}

	public void calculateVariants() {
		buildVariants.get().forEach(buildVariant -> {
			val variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, buildVariants.get()).withComponentIdentifier(component.getIdentifier()).withType(JniLibraryInternal.class).build();

			val dependencies = newDependencies(buildVariant, component, variantIdentifier);
			variantCollection.registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));
		});
	}

	private JniLibraryInternal createVariant(VariantIdentifier<JniLibraryInternal> identifier, VariantComponentDependencies variantDependencies) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.getDimensions().size() >= 2);
		Preconditions.checkArgument(buildVariant.getDimensions().get(0) instanceof OperatingSystemFamily);
		Preconditions.checkArgument(buildVariant.getDimensions().get(1) instanceof MachineArchitecture);

		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(LifecycleBasePlugin.ASSEMBLE_TASK_NAME), identifier), task -> {
			task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
			task.setDescription(String.format("Assembles the '%s' outputs of this project.", BuildVariantNamer.INSTANCE.determineName((BuildVariantInternal)identifier.getBuildVariant())));
		});

		val result = new JniLibraryInternal(identifier, component.getSources(), component.getGroupId(), variantDependencies, objectFactory, configurationContainer, providerFactory, taskRegistry, assembleTask, eventPublisher, binaryViewFactory, taskViewFactory);
		return result;
	}

	private VariantComponentDependencies newDependencies(BuildVariantInternal buildVariant, JniLibraryComponentInternal component, VariantIdentifier<JniLibraryInternal> variantIdentifier) {
		DefaultJavaNativeInterfaceNativeComponentDependencies variantDependencies = component.getDependencies();
		if (component.getBuildVariants().get().size() > 1) {
			val dependencyContainer = objectFactory.newInstance(DefaultComponentDependencies.class, variantIdentifier, new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(configurationContainer), dependencyHandler));
			variantDependencies = objectFactory.newInstance(DefaultJavaNativeInterfaceNativeComponentDependencies.class, dependencyContainer);
			variantDependencies.configureEach(variantBucket -> {
				component.getDependencies().findByName(variantBucket.getName()).ifPresent(componentBucket -> {
					variantBucket.getAsConfiguration().extendsFrom(componentBucket.getAsConfiguration());
				});
			});
		}

		val incomingDependenciesBuilder = DefaultNativeIncomingDependencies.builder(new NativeComponentDependenciesJavaNativeInterfaceAdapter(variantDependencies)).withVariant(buildVariant);
		boolean hasSwift = languageSourceSetRepository.anyKnownIdentifier(withType(SwiftSourceSet.class));
		boolean hasHeader = languageSourceSetRepository.anyKnownIdentifier(withType(CSourceSet.class).or(withType(CppSourceSet.class)).or(withType(ObjectiveCSourceSet.class)).or(withType(ObjectiveCppSourceSet.class)));
		if (hasSwift) {
			incomingDependenciesBuilder.withIncomingSwiftModules();
		} else if (hasHeader) {
			incomingDependenciesBuilder.withIncomingHeaders();
		}

		NativeIncomingDependencies incoming = incomingDependenciesBuilder.buildUsing(objectFactory);

		return new VariantComponentDependencies(variantDependencies, incoming);
	}
}
