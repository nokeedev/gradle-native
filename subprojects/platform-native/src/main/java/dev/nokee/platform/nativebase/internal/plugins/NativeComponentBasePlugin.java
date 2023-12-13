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

import dev.nokee.language.nativebase.internal.ConfigurationUtilsEx;
import dev.nokee.language.nativebase.internal.HasLinkElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.model.internal.KnownModelObject;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.HasApiDependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTask;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInternal;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.plugins.RegisterVariants;
import dev.nokee.platform.base.internal.rules.ExtendsFromImplementationDependencyBucketAction;
import dev.nokee.platform.base.internal.rules.ExtendsFromParentDependencyBucketAction;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplication;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibrary;
import dev.nokee.platform.nativebase.internal.HasBinaryLifecycleTask;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
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
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.Sync;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
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
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;
import static dev.nokee.utils.TransformerUtils.to;
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

		model(project, objects()).whenElementKnown(it -> {
			if (it.getType().isSubtypeOf(DependencyAwareComponent.class)) {
				try {
					Method getDependencies = it.getType().getConcreteType().getMethod("getDependencies");
					Set<String> buckets = new LinkedHashSet<>();
					for (Method method : getDependencies.getReturnType().getMethods()) {
						if (DeclarableDependencyBucket.class.isAssignableFrom(method.getReturnType())) {
							if (buckets.add(method.getName())) {
								final String elementName = StringUtils.uncapitalize(method.getName().substring("get".length()));
								final String configurationName = ModelObjectIdentifiers.asQualifyingName(it.getIdentifier().child(elementName)).toString();
								final Configuration configuration = project.getConfigurations().maybeCreate(configurationName);
								configuration.setCanBeConsumed(false);
								configuration.setCanBeResolved(false);
								configuration.withDependencies(__ -> it.realizeNow());
							}
						}
					}
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
		});

		variants(project).configureEach(variant -> {
			if (variant instanceof DependencyAwareComponent && ((DependencyAwareComponent<?>) variant).getDependencies() instanceof NativeComponentDependencies) {
				final NativeComponentDependencies dependencies = ((DependencyAwareComponent<NativeComponentDependencies>) variant).getDependencies();
				if (variant instanceof HasApiDependencyBucket) {
					((DependencyBucketInternal) dependencies.getImplementation()).extendsFrom(((HasApiDependencyBucket) variant).getApi());
				}
			}
		});
		model(project, mapOf(Variant.class)).whenElementKnown(HasRuntimeElementsDependencyBucket.class, new Action<KnownModelObject<HasRuntimeElementsDependencyBucket>>() {
			@Override
			public void execute(KnownModelObject<HasRuntimeElementsDependencyBucket> knownVariant) {
				final VariantIdentifier variantIdentifier = (VariantIdentifier) knownVariant.getIdentifier();
				final Configuration runtimeElements = project.getConfigurations().maybeCreate(ModelObjectIdentifiers.asFullyQualifiedName(variantIdentifier.child("runtimeElements")).toString());

				runtimeElements.setCanBeConsumed(true);
				runtimeElements.setCanBeResolved(false);

				project.getConfigurations().matching(it -> {
					return it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(knownVariant.getIdentifier().child("implementation")).toString()) || it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(knownVariant.getIdentifier().child("runtimeOnly")).toString());
				}).all(runtimeElements::extendsFrom);

				ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))).execute(runtimeElements);
				ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variantIdentifier.getBuildVariant(), project.getObjects()).execute(runtimeElements);

				final BuildVariantInternal buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
				if (!buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).isStatic()) {
					final ModelObject<Sync> syncRuntimeLibraryTask = model(project, registryOf(Task.class)).register(knownVariant.getIdentifier().child(TaskName.of("sync", "runtimeLibrary")), Sync.class);
					val runtimeLibraryName = finalizeValueOnRead(project.getObjects().property(String.class).value(project.provider(() -> {
						val toolChainSelector = new ToolChainSelectorInternal(((ProjectInternal) project).getModelRegistry());
						val platform = NativePlatformFactory.create(buildVariant.getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS));
						val toolchain = toolChainSelector.select(platform);
						val toolProvider = toolchain.select(platform);
						val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
						val baseName = knownVariant.asProvider().map(to(HasBaseName.class)).flatMap(HasBaseName::getBaseName).get();
						if (linkage.isExecutable()) {
							return toolProvider.getExecutableName(baseName);
						} else if (linkage.isShared()) {
							return toolProvider.getSharedLibraryName(baseName);
						} else {
							throw new UnsupportedOperationException();
						}
					})));

					syncRuntimeLibraryTask.configure(task -> {
						task.from(knownVariant.asProvider().map(to(HasDevelopmentBinary.class)).flatMap(HasDevelopmentBinary::getDevelopmentBinary).flatMap(this::getOutgoingRuntimeLibrary), spec -> spec.rename(it -> runtimeLibraryName.get()));
						task.setDestinationDir(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task)).get().getAsFile());
					});

					runtimeElements.getOutgoing().artifact(syncRuntimeLibraryTask.asProvider().map(it -> new File(it.getDestinationDir(), runtimeLibraryName.get())));
				}
			}

			private Provider<RegularFile> getOutgoingRuntimeLibrary(Binary binary) {
				if (binary instanceof NativeExecutableBinarySpec) {
					return ((NativeExecutableBinarySpec) binary).getLinkTask().flatMap(LinkExecutable::getLinkedFile);
				} else if (binary instanceof NativeSharedLibraryBinarySpec) {
					return ((NativeSharedLibraryBinarySpec) binary).getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile);
				} else if (binary instanceof StaticLibraryBinary) {
					throw new UnsupportedOperationException();
				} else if (binary instanceof HasOutputFile) {
					return ((HasOutputFile) binary).getOutputFile();
				}
				throw new IllegalArgumentException("Unsupported binary to export");
			}
		});
		model(project, mapOf(Variant.class)).whenElementKnown(HasLinkElementsDependencyBucket.class, new Action<KnownModelObject<HasLinkElementsDependencyBucket>>() {
			@Override
			public void execute(KnownModelObject<HasLinkElementsDependencyBucket> knownVariant) {
				final VariantIdentifier variantIdentifier = (VariantIdentifier) knownVariant.getIdentifier();
				final Configuration linkElements = project.getConfigurations().maybeCreate(ModelObjectIdentifiers.asFullyQualifiedName(variantIdentifier.child("linkElements")).toString());

				linkElements.setCanBeConsumed(true);
				linkElements.setCanBeResolved(false);

				project.getConfigurations().matching(it -> {
					// TODO: We should extends from LinkOnlyApi
					return it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(knownVariant.getIdentifier().child("implementation")).toString());
				}).all(linkElements::extendsFrom);

				ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))).execute(linkElements);
				ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variantIdentifier.getBuildVariant(), project.getObjects()).execute(linkElements);

				final BuildVariantInternal buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
				final ModelObject<Sync> syncLinkLibraryTask = model(project, registryOf(Task.class)).register(knownVariant.getIdentifier().child(TaskName.of("sync", "linkLibrary")), Sync.class);
				val linkLibraryName = finalizeValueOnRead(project.getObjects().property(String.class).value(project.provider(() -> {
					val toolChainSelector = new ToolChainSelectorInternal(((ProjectInternal) project).getModelRegistry());
					val platform = NativePlatformFactory.create(buildVariant.getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS));
					val toolchain = toolChainSelector.select(platform);
					val toolProvider = toolchain.select(platform);
					val linkage = buildVariant.getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
					val baseName = knownVariant.asProvider().map(to(HasBaseName.class)).flatMap(HasBaseName::getBaseName).get();
					if (linkage.isStatic()) {
						return toolProvider.getStaticLibraryName(baseName);
					} else if (linkage.isShared()) {
						if (toolProvider.producesImportLibrary()) {
							return toolProvider.getImportLibraryName(baseName);
						} else {
							return toolProvider.getSharedLibraryLinkFileName(baseName);
						}
					} else {
						throw new UnsupportedOperationException();
					}
				})));
				syncLinkLibraryTask.configure(task -> {
					task.from(knownVariant.asProvider().map(to(HasDevelopmentBinary.class)).flatMap(HasDevelopmentBinary::getDevelopmentBinary).flatMap(this::getOutgoingLinkLibrary), spec -> spec.rename(it -> linkLibraryName.get()));
					task.setDestinationDir(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task)).get().getAsFile());
				});
				linkElements.getOutgoing().artifact(syncLinkLibraryTask.asProvider().map(it -> new File(it.getDestinationDir(), linkLibraryName.get())));
			}

			private Provider<RegularFile> getOutgoingLinkLibrary(Binary binary) {
				if (binary instanceof NativeSharedLibraryBinarySpec) {
					return ((NativeSharedLibraryBinarySpec) binary).getLinkTask().flatMap(it -> it.getImportLibrary().orElse(it.getLinkedFile()));
				} else if (binary instanceof StaticLibraryBinary) {
					return ((StaticLibraryBinary) binary).getCreateTask().flatMap(CreateStaticLibrary::getOutputFile);
				} else if (binary instanceof HasOutputFile) {
					return ((HasOutputFile) binary).getOutputFile();
				}
				throw new IllegalArgumentException("Unsupported binary to export");
			}
		});

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
