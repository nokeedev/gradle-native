/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceAwareComponent;
import dev.nokee.language.nativebase.internal.ConfigurationUtilsEx;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.HasLinkElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.model.internal.KnownModelObject;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.HasApiDependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTask;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInternal;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.RegisterVariants;
import dev.nokee.platform.base.internal.rules.ExtendsFromImplementationDependencyBucketAction;
import dev.nokee.platform.base.internal.rules.ExtendsFromParentDependencyBucketAction;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplication;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibrary;
import dev.nokee.platform.nativebase.internal.HasBinaryLifecycleTask;
import dev.nokee.platform.nativebase.internal.HasRuntimeLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.NativeBundleBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeComponentSpec;
import dev.nokee.platform.nativebase.internal.NativeComponentSpecEx;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.NativeSharedLibraryBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeStaticLibraryBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeVariantSpec;
import dev.nokee.platform.nativebase.internal.ObjectsTaskMixIn;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfigurationRegistrationRule;
import dev.nokee.platform.nativebase.internal.archiving.NativeArchiveCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.compiling.NativeCompileCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.dependencies.NativeApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.mixins.LinkOnlyDependencyBucketMixIn;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantTransformer;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.internal.rules.TargetBuildTypeConventionRule;
import dev.nokee.platform.nativebase.internal.rules.TargetMachineConventionRule;
import dev.nokee.platform.nativebase.internal.rules.TargetedNativeComponentDimensionsRule;
import dev.nokee.platform.nativebase.internal.rules.ToBinariesCompileTasksTransformer;
import dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer;
import dev.nokee.platform.nativebase.internal.services.UnbuildableWarningService;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.ModelElementSupport.safeAsModelElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.artifacts;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.utils.BuildServiceUtils.registerBuildServiceIfAbsent;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static java.util.Collections.singletonList;

public class NativeComponentBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(NativeRuntimePlugin.class);
		project.getPluginManager().apply(DarwinRuntimePlugin.class); // for now, later we will be more smart
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, withElement(new ExtendsFromParentDependencyBucketAction<LinkOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(LinkOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getLinkOnly();
			}
		})));
		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromImplementationDependencyBucketAction<LinkOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(LinkOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getLinkOnly();
			}
		}));

		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultNativeApplication.class);
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeApplication.Variant.class);
		model(project, factoryRegistryOf(Component.class)).registerFactory(DefaultNativeLibrary.class);
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeLibrary.Variant.class);
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeSharedLibraryBinarySpec.class);
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeBundleBinarySpec.class);
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeExecutableBinarySpec.class);
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeStaticLibraryBinarySpec.class);
		model(project, mapOf(Variant.class)).configureEach(NativeComponentSpecEx.class, result -> {
			result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(((VariantInternal) result).getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
		});

		components(project).configureEach(new LegacyFrameworkAwareDependencyBucketAction<>(project.getObjects()));
		variants(project).configureEach(new LinkLibrariesExtendsFromParentDependencyBucketAction<>());
		variants(project).configureEach(new RuntimeLibrariesExtendsFromParentDependencyBucketAction<>());

		variants(project).configureEach(variant -> {
			if (variant instanceof SourceAwareComponent) {
				final View<LanguageSourceSet> sources = ((SourceAwareComponent) variant).getSources();
				sources.configureEach(sourceSet -> {
					if (sourceSet instanceof HasHeaderSearchPaths) {
						final Configuration headerSearchPaths = ((HasHeaderSearchPaths) sourceSet).getHeaderSearchPaths().getAsConfiguration();
						ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) variant.getBuildVariant(), project.getObjects()).execute(headerSearchPaths);
						ConfigurationUtilsEx.configureAsGradleDebugCompatible(headerSearchPaths);
					}
				});
			}
		});

		variants(project).configureEach(variant -> {
			ModelElementSupport.safeAsModelElement(variant).map(ModelElement::getIdentifier).ifPresent(variantIdentifier -> {
				model(project, mapOf(Task.class)).configureEach(AbstractNativeCompileTask.class, task -> {
					ModelElementSupport.safeAsModelElement(task).map(ModelElement::getIdentifier).ifPresent(taskIdentifier -> {
						if (ModelObjectIdentifiers.descendantOf(taskIdentifier, variantIdentifier)) {
							NativePlatformFactory.create(variant.getBuildVariant()).ifPresent(task.getTargetPlatform()::set);
						}
					});
				});
				model(project, mapOf(Task.class)).configureEach(SwiftCompile.class, task -> {
					ModelElementSupport.safeAsModelElement(task).map(ModelElement::getIdentifier).ifPresent(taskIdentifier -> {
						if (ModelObjectIdentifiers.descendantOf(taskIdentifier, variantIdentifier)) {
							NativePlatformFactory.create(variant.getBuildVariant()).ifPresent(task.getTargetPlatform()::set);
						}
					});
				});
			});
		});

		variants(project).withType(NativeVariantSpec.class).configureEach(variant -> {
			val buildVariant = variant.getBuildVariant();
			val identifier = ((ModelElement) variant).getIdentifier();

			if (buildVariant.hasAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
				final BinaryLinkage linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
				if (linkage.isExecutable()) {
					((ExtensionAware) variant).getExtensions().add("executable", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("executable")), NativeExecutableBinarySpec.class).asProvider());
				} else if (linkage.isShared()) {
					((ExtensionAware) variant).getExtensions().add("sharedLibrary", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("sharedLibrary")), NativeSharedLibraryBinarySpec.class).asProvider());
				} else if (linkage.isBundle()) {
					((ExtensionAware) variant).getExtensions().add("bundle", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("bundle")), NativeBundleBinarySpec.class).asProvider());
				} else if (linkage.isStatic()) {
					((ExtensionAware) variant).getExtensions().add("staticLibrary", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("staticLibrary")), NativeStaticLibraryBinarySpec.class).asProvider());
				}

				if (linkage.isShared() || linkage.isStatic()) {
					project.getTasks().withType(SwiftCompile.class).configureEach(task -> {
						ModelElementSupport.safeAsModelElement(task).map(ModelElement::getIdentifier).ifPresent(taskIdentifier -> {
							// TODO: Should check under the binary not the variant
							if (ModelObjectIdentifiers.descendantOf(taskIdentifier, identifier)) {
								task.getCompilerArgs().add("-parse-as-library");
							}
						});
					});
				}
			}
		});

		artifacts(project).configureEach(new RuntimeLibrariesConfigurationRegistrationRule(model(project, objects()), project.getObjects()));
		variants(project).configureEach(new AttachAttributesToConfigurationRule(HasRuntimeLibrariesDependencyBucket.class, HasRuntimeLibrariesDependencyBucket::getRuntimeLibraries, project.getObjects(), model(project, mapOf(Artifact.class))));

		variants(project).configureEach(variant -> {
			if (variant instanceof DependencyAwareComponent && ((DependencyAwareComponent<?>) variant).getDependencies() instanceof NativeComponentDependencies) {
				final NativeComponentDependencies dependencies = ((DependencyAwareComponent<NativeComponentDependencies>) variant).getDependencies();

				if (variant instanceof HasRuntimeElementsDependencyBucket) {
					final ConsumableDependencyBucketSpec runtimeElements = ((HasRuntimeElementsDependencyBucket) variant).getRuntimeElements();
					runtimeElements.extendsFrom(dependencies.getImplementation(), dependencies.getRuntimeOnly());
				}

				if (variant instanceof HasLinkElementsDependencyBucket) {
					final ConsumableDependencyBucketSpec runtimeElements = ((HasLinkElementsDependencyBucket) variant).getLinkElements();
					// TODO: We should extends from LinkOnlyApi
					runtimeElements.extendsFrom(dependencies.getImplementation());
				}

				if (variant instanceof HasApiDependencyBucket) {
					((DependencyBucketInternal) dependencies.getImplementation()).extendsFrom(((HasApiDependencyBucket) variant).getApi());
				}
			}
		});
		variants(project).configureEach(withElement((element, variant) -> {
			if (variant instanceof HasRuntimeElementsDependencyBucket) {
				final ConsumableDependencyBucketSpec bucket = ((HasRuntimeElementsDependencyBucket) variant).getRuntimeElements();
				ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))).execute(bucket.getAsConfiguration());
				ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variant.getBuildVariant(), project.getObjects()).execute(bucket.getAsConfiguration());
			}

			if (variant instanceof HasLinkElementsDependencyBucket) {
				final ConsumableDependencyBucketSpec bucket = ((HasLinkElementsDependencyBucket) variant).getLinkElements();
				ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))).execute(bucket.getAsConfiguration());
				ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variant.getBuildVariant(), project.getObjects()).execute(bucket.getAsConfiguration());
			}
		}));
		variants(project).withType(NativeVariantSpec.class).configureEach(variant -> {
			if (variant instanceof NativeApplicationComponent && variant instanceof HasRuntimeElementsDependencyBucket) {
				final NativeApplicationOutgoingDependencies outgoing = new NativeApplicationOutgoingDependencies(variant, ((HasRuntimeElementsDependencyBucket) variant).getRuntimeElements().getAsConfiguration(), project);
				outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
			}
		});
		variants(project).withType(NativeVariantSpec.class).configureEach(withElement((element, variant) -> {
			if (variant instanceof NativeLibraryComponent) {
				final NativeOutgoingDependencies outgoing = new NativeLibraryOutgoingDependencies(variant.getIdentifier(), ((HasLinkElementsDependencyBucket) variant).getLinkElements().getAsConfiguration(), ((HasRuntimeElementsDependencyBucket) variant).getRuntimeElements().getAsConfiguration(), project, ((HasBaseName) variant).getBaseName());
				outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
			}
		}));

		project.getPluginManager().apply(NativeCompileCapabilityPlugin.class);
		project.getPluginManager().apply(NativeLinkCapabilityPlugin.class);
		project.getPluginManager().apply(NativeArchiveCapabilityPlugin.class);

		components(project).withType(NativeComponentSpec.class)
			.configureEach(new TargetedNativeComponentDimensionsRule(project.getObjects().newInstance(ToolChainSelectorInternal.class)));
		model(project, objects()).configureEach(ofType(TargetMachineAwareComponent.class, withElement(new TargetMachineConventionRule(project.getProviders()))));
		model(project, objects()).configureEach(ofType(TargetBuildTypeAwareComponent.class, withElement(new TargetBuildTypeConventionRule(project.getProviders()))));
		components(project).configureEach(component -> {
			if (component instanceof NativeApplicationComponent && component instanceof TargetLinkageAwareComponent) {
				((TargetLinkageAwareComponent) component).getTargetLinkages().convention(singletonList(TargetLinkages.EXECUTABLE));
			}
		});
		components(project).configureEach(component -> {
			if (component instanceof NativeLibraryComponent && component instanceof TargetLinkageAwareComponent) {
				((TargetLinkageAwareComponent) component).getTargetLinkages().convention(singletonList(TargetLinkages.SHARED));
			}
		});
		model(project).getExtensions().add("__nokeeService_targetMachineFactory", NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY);
		model(project).getExtensions().add("__nokeeService_targetBuildTypeFactory", NativeRuntimeBasePlugin.TARGET_BUILD_TYPE_FACTORY);
		model(project).getExtensions().add("__nokeeService_targetLinkageFactory", NativeRuntimeBasePlugin.TARGET_LINKAGE_FACTORY);

		model(project, mapOf(Component.class)).whenElementFinalized(TargetMachineAwareComponent.class, component -> {
			component.getTargetMachines().disallowChanges();
		});
		model(project, mapOf(Component.class)).whenElementFinalized(TargetLinkageAwareComponent.class, component -> {
			component.getTargetLinkages().disallowChanges();
		});
		model(project, mapOf(Component.class)).whenElementFinalized(TargetBuildTypeAwareComponent.class, component -> {
			component.getTargetBuildTypes().disallowChanges();
		});

		val unbuildableWarningService = forUseAtConfigurationTime(registerBuildServiceIfAbsent(project, UnbuildableWarningService.class));
		components(project).configureEach(component -> {
			if (component instanceof HasAssembleTask && component instanceof HasDevelopmentVariant) {
				// The "component" assemble task was most likely added by the 'lifecycle-base' plugin
				//   then we configure the dependency.
				//   Note that the dependency may already exists for single variant component but it's not a big deal.
				final Provider<? extends Variant> developmentVariant = ((HasDevelopmentVariant<?>) component).getDevelopmentVariant();
				((HasAssembleTask) component).getAssembleTask().configure(configureDependsOn(developmentVariant.flatMap(ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY).map(Arrays::asList)
					.orElse(unbuildableWarningService.map(it -> {
						it.warn(safeAsModelElement(component).map(ModelElement::getIdentifier).orElseThrow(RuntimeException::new));
						return Collections.emptyList();
					}))));
			}
		});

		model(project, objects())
			.whenElementFinalized(ofType(VariantComponentSpec.class, new RegisterVariants<>(model(project, registryOf(Variant.class)))));

		// TODO: Should be part of native-library-base/native-application-base plugin
		variants(project).configureEach(ofType(HasBinaryLifecycleTask.class, withElement((element, variant, target) -> {
			target.getBinaryLifecycleTask().configure(configureBuildGroup());
			target.getBinaryLifecycleTask().configure(task -> {
				val linkage = ((BuildVariantInternal) variant.getBuildVariant()).getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
				NamedDomainObjectProvider<Binary> binaryProvider;// = artifacts(project).named(variant.getName());
				if (linkage.isShared()) {
					binaryProvider = artifacts(project).named(ModelObjectIdentifiers.asFullyQualifiedName(element.getIdentifier().child("sharedLibrary")).toString(), Binary.class);
					task.setDescription(String.format("Assembles a shared library binary containing the objects files of %s.", "shared library binary '" + binaryProvider.getName() + "'"));
				} else if (linkage.isExecutable()) {
					binaryProvider = artifacts(project).named(ModelObjectIdentifiers.asFullyQualifiedName(element.getIdentifier().child("executable")).toString(), Binary.class);
					task.setDescription(String.format("Assembles a executable binary containing the objects files of %s.", "executable binary '" + binaryProvider.getName() + "'"));
				} else {
					binaryProvider = artifacts(project).named(ModelObjectIdentifiers.asFullyQualifiedName(element.getIdentifier().child("staticLibrary")).toString(), Binary.class);
					task.setDescription(String.format("Assembles a static library binary containing the objects files of %s.", "static library binary '" + binaryProvider.getName() + "'"));
				}
				task.dependsOn((Callable<Object>) binaryProvider::get);
			});
		})));

		model(project, mapOf(Component.class)).configureEach(VariantComponentSpec.class, component -> {
			final Provider<Set<KnownModelObject<VariantInternal>>> variants = model(project, objects()).getElements(VariantInternal.class, it -> {
				if (ModelObjectIdentifiers.descendantOf(it.getIdentifier(), component.getIdentifier())) {
					return it.getType().isSubtypeOf(VariantInternal.class);
				}
				return false;
			});
			component.getDevelopmentVariant().convention(variants.flatMap(new BuildableDevelopmentVariantTransformer<>()));
		});
		variants(project).configureEach(ofType(HasAssembleTask.class, withElement((element, variant, target) -> {
			if (!((VariantIdentifier) element.getIdentifier()).getUnambiguousName().isEmpty()) {
				target.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		})));
		variants(project).configureEach(ofType(ObjectsTaskMixIn.class, withElement((element, variant, target) -> {
			if (!((VariantIdentifier) element.getIdentifier()).getUnambiguousName().isEmpty()) {
				target.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		})));

		// TODO: Restrict this rule
		components(project).configureEach(component -> {
			if (component instanceof ObjectsTaskMixIn && component instanceof HasDevelopmentVariant) {
				((ObjectsTaskMixIn) component).getObjectsTask().configure(configureDependsOn(((HasDevelopmentVariant<?>) component).getDevelopmentVariant().flatMap(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS)));
			}
		});

		model(project, objects()).configureEach(ofType(AbstractLinkTask.class, withElement(new BiConsumer<ModelElement, AbstractLinkTask>() {
			@Override
			public void accept(ModelElement identity, AbstractLinkTask task) {
				identity.getParents().filter(this::isNativeLinkableBinary).findFirst().ifPresent(__ -> {
					// Until we model the build type
					task.getDebuggable().set(false);
				});
			}

			private boolean isNativeLinkableBinary(ModelElement it) {
				return it.instanceOf(NativeBundleBinarySpec.class) || it.instanceOf(NativeExecutableBinarySpec.class) || it.instanceOf(NativeSharedLibraryBinarySpec.class);
			}
		})));
		artifacts(project).withType(NativeSharedLibraryBinarySpec.class).configureEach(binary -> {
			binary.getLinkTask().configure(task -> {
				final Provider<String> installName = task.getLinkedFile().getLocationOnly().map(linkedFile -> linkedFile.getAsFile().getName());
				task.getInstallName().set(installName);
			});
		});
		artifacts(project).withType(NativeSharedLibraryBinarySpec.class).configureEach(binary -> {
			binary.getLinkedFile().set(binary.getLinkTask().flatMap(AbstractLinkTask::getLinkedFile));
			binary.getLinkedFile().disallowChanges();
			binary.getLinkedFile().finalizeValueOnRead();
		});
	}
}
