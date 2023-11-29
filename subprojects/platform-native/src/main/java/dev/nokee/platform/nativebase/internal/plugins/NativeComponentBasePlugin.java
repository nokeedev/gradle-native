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

import com.google.common.base.Preconditions;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.nativebase.internal.HasApiElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.HasLinkElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.HasRuntimeElementsDependencyBucket;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.swift.internal.plugins.HasImportModules;
import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetSpec;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjects;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.HasApiDependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
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
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationVariant;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryVariant;
import dev.nokee.platform.nativebase.internal.HasRuntimeLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.NativeBundleBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeComponentSpec;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.NativeSharedLibraryBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeStaticLibraryBinarySpec;
import dev.nokee.platform.nativebase.internal.NativeVariant;
import dev.nokee.platform.nativebase.internal.ObjectsTaskMixIn;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfigurationRegistrationRule;
import dev.nokee.platform.nativebase.internal.archiving.NativeArchiveCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.compiling.NativeCompileCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.NativeApplicationOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.SwiftLibraryOutgoingDependencies;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkCapabilityPlugin;
import dev.nokee.platform.nativebase.internal.mixins.LinkOnlyDependencyBucketMixIn;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
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
import dev.nokee.utils.TextCaseUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.Sync;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.model.internal.ModelElementSupport.safeAsModelElement;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
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

		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new dev.nokee.platform.base.internal.rules.ExtendsFromParentDependencyBucketAction<LinkOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(LinkOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getLinkOnly();
			}
		});
		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromImplementationDependencyBucketAction<LinkOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(LinkOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getLinkOnly();
			}
		});

		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeSharedLibraryBinarySpec.class, name -> {
			return instantiator(project).newInstance(NativeSharedLibraryBinarySpec.class);
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeBundleBinarySpec.class, name -> {
			return instantiator(project).newInstance(NativeBundleBinarySpec.class);
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeExecutableBinarySpec.class, name -> {
			return instantiator(project).newInstance(NativeExecutableBinarySpec.class);
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(NativeStaticLibraryBinarySpec.class, name -> {
			return instantiator(project).newInstance(NativeStaticLibraryBinarySpec.class);
		});
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeApplicationVariant.class, name -> {
			return instantiator(project).newInstance(DefaultNativeApplicationVariant.class);
		});
		variants(project).withType(DefaultNativeApplicationVariant.class).configureEach(result -> {
			result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(result.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
		});
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeLibraryVariant.class, name -> {
			return instantiator(project).newInstance(DefaultNativeLibraryVariant.class);
		});
		variants(project).withType(DefaultNativeLibraryVariant.class).configureEach(result -> {
			result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(result.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
		});


		project.afterEvaluate(__ -> {
			// TODO: Should not force realize consumable bucket if Gradle can handle it.
			variants(project).configureEach(variant -> {
				if (variant instanceof HasApiElementsDependencyBucket) {
					((HasApiElementsDependencyBucket) variant).getApiElements().getAsConfiguration().getOutgoing().getArtifacts().all(___ -> {});
				}

				if (variant instanceof HasLinkElementsDependencyBucket) {
					((HasLinkElementsDependencyBucket) variant).getLinkElements().getAsConfiguration().getOutgoing().getArtifacts().all(___ -> {});
				}

				if (variant instanceof HasRuntimeElementsDependencyBucket) {
					((HasRuntimeElementsDependencyBucket) variant).getRuntimeElements().getAsConfiguration().getOutgoing().getArtifacts().all(___ -> {});
				}
			});
		});

		components(project).configureEach(new LegacyFrameworkAwareDependencyBucketAction<>(project.getObjects()));
		variants(project).configureEach(new LinkLibrariesExtendsFromParentDependencyBucketAction<>());
		variants(project).configureEach(new RuntimeLibrariesExtendsFromParentDependencyBucketAction<>());

		variants(project).configureEach(variant -> {
			if (variant instanceof SourceAwareComponent && ((SourceAwareComponent<?>) variant).getSources() instanceof View) {
				final View<LanguageSourceSet> sources = (View<LanguageSourceSet>) ((SourceAwareComponent<?>) variant).getSources();
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
			if (variant instanceof SourceAwareComponent && ((SourceAwareComponent<?>) variant).getSources() instanceof View) {
				final View<LanguageSourceSet> sources = (View<LanguageSourceSet>) ((SourceAwareComponent<?>) variant).getSources();
				sources.configureEach(sourceSet -> {
					if (sourceSet instanceof HasImportModules) {
						final Configuration importModules = ((HasHeaderSearchPaths) sourceSet).getHeaderSearchPaths().getAsConfiguration();
						ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) variant.getBuildVariant(), project.getObjects()).execute(importModules);
						ConfigurationUtilsEx.configureAsGradleDebugCompatible(importModules);
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
				model(project, mapOf(Task.class)).configureEach(SwiftCompileTask.class, task -> {
					ModelElementSupport.safeAsModelElement(task).map(ModelElement::getIdentifier).ifPresent(taskIdentifier -> {
						if (ModelObjectIdentifiers.descendantOf(taskIdentifier, variantIdentifier)) {
							NativePlatformFactory.create(variant.getBuildVariant()).ifPresent(task.getTargetPlatform()::set);
						}
					});
				});
			});
		});

		variants(project).withType(NativeVariant.class).configureEach(variant -> {
			val buildVariant = (BuildVariantInternal) variant.getBuildVariant();
			val identifier = ((ModelElement) variant).getIdentifier();

			if (buildVariant.hasAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)) {
				sources(project).withType(SwiftSourceSetSpec.class).configureEach(sourceSet -> {
					if (ModelObjectIdentifiers.descendantOf(sourceSet.getIdentifier(), identifier)) {
						sourceSet.getCompileTask().configure(task -> {
							ModelElementSupport.safeAsModelElement(task).map(ModelElement::getIdentifier).ifPresent(id -> {
								task.getModuleName().set(project.provider(() -> {
									return model(project, objects()).parentsOf(id)
										.filter(it -> it.instanceOf(HasBaseName.class))
										.map(it -> (Provider<String>) it.asModelObject(HasBaseName.class).get().getBaseName())
										.findFirst()
										.orElseGet(() -> project.provider(() -> null));
								}).flatMap(it -> it).map(TextCaseUtils::toCamelCase));
							});
						});
					}
				});

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
					project.getTasks().withType(SwiftCompileTask.class).configureEach(task -> {
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

				if (variant instanceof HasApiElementsDependencyBucket) {
					final ConsumableDependencyBucketSpec apiElements = ((HasApiElementsDependencyBucket) variant).getApiElements();

					// TODO: We should extends from CompileOnlyApi
					if (variant instanceof HasApiDependencyBucket) {
						apiElements.extendsFrom(((HasApiDependencyBucket) variant).getApi());
					}
				}

				if (variant instanceof HasApiDependencyBucket) {
					((DependencyBucketInternal) dependencies.getImplementation()).extendsFrom(((HasApiDependencyBucket) variant).getApi());
				}
			}
		});
		variants(project).configureEach(variant -> {
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

			if (variant instanceof HasApiElementsDependencyBucket) {
				final ConsumableDependencyBucketSpec bucket = ((HasApiElementsDependencyBucket) variant).getApiElements();
				ModelElementSupport.safeAsModelElement(variant).map(ModelElement::getIdentifier).ifPresent(identifier -> {
					final boolean hasSwift = model(project, objects()).parentsOf(identifier).anyMatch(it -> ((ExtensionAware) it.get()).getExtensions().findByType(SupportSwiftSourceSetTag.class) != null) || project.getExtensions().findByType(SupportSwiftSourceSetTag.class) != null;
					if (hasSwift) {
						ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.SWIFT_API))).execute(bucket.getAsConfiguration());
						ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variant.getBuildVariant(), project.getObjects()).execute(bucket.getAsConfiguration());
						ConfigurationUtilsEx.configureAsGradleDebugCompatible(bucket.getAsConfiguration());
					} else {
						ConfigurationUtils.<Configuration>configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))).execute(bucket.getAsConfiguration());
						ConfigurationUtilsEx.configureOutgoingAttributes((BuildVariantInternal) variant.getBuildVariant(), project.getObjects()).execute(bucket.getAsConfiguration());
						ConfigurationUtilsEx.configureAsGradleDebugCompatible(bucket.getAsConfiguration());
					}
				});

			}
		});
		variants(project).withType(DefaultNativeApplicationVariant.class).configureEach(variant -> {
			new NativeApplicationOutgoingDependencies(variant.getRuntimeElements().getAsConfiguration(), project.getObjects()).getExportedBinary().convention(variant.getDevelopmentBinary());
		});
		variants(project).withType(DefaultNativeLibraryVariant.class).configureEach(new Action<DefaultNativeLibraryVariant>() {
			@Override
			public void execute(DefaultNativeLibraryVariant variant) {
				final VariantIdentifier variantIdentifier = variant.getIdentifier();
				final boolean hasSwift = model(project, objects()).parentsOf(variantIdentifier).anyMatch(it -> ((ExtensionAware) it.get()).getExtensions().findByType(SupportSwiftSourceSetTag.class) != null) || project.getExtensions().findByType(SupportSwiftSourceSetTag.class) != null;
				NativeOutgoingDependencies outgoing = null;
				if (hasSwift) {
					outgoing = new SwiftLibraryOutgoingDependencies(variant.getApiElements().getAsConfiguration(), variant.getLinkElements().getAsConfiguration(), variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
				} else {
					outgoing = new NativeLibraryOutgoingDependencies(variant.getApiElements().getAsConfiguration(), variant.getLinkElements().getAsConfiguration(), variant.getRuntimeElements().getAsConfiguration(), project.getObjects());
				}

				if (hasSwift) {
					outgoing.getExportedSwiftModule().convention(project.provider(() -> {
						List<? extends Provider<RegularFile>> result = variant.getBinaries().withType(NativeBinary.class).flatMap(binary -> {
							List<? extends Provider<RegularFile>> modules = binary.getCompileTasks().withType(SwiftCompileTask.class).map(task -> task.getModuleFile()).get();
							return modules;
						}).get();
						return one(result);
					}).flatMap(it -> it));
				}
				val syncTask = project.getTasks().register("sync" + StringUtils.capitalize(variantIdentifier.getUnambiguousName()) + "PublicHeaders", Sync.class, task -> {
					task.from((Callable<?>) () -> {
						return model(project, objects()).parentsOf(variantIdentifier)
							.map(it -> it.get())
							.flatMap(it -> {
								final FileCollection publicHeaders = (FileCollection) ((ExtensionAware) it).getExtensions().findByName("publicHeaders");
								if (publicHeaders == null) {
									return Stream.empty();
								} else {
									return Stream.of(publicHeaders);
								}
							})
							.findFirst().map(Object.class::cast).orElseGet(Collections::emptyList);
					});
					task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
				});
				outgoing.getExportedHeaders().fileProvider(syncTask.map(it -> it.getDestinationDir()));
				outgoing.getExportedBinary().convention(variant.getDevelopmentBinary());
			}

			private <T> T one(Iterable<T> c) {
				Iterator<T> iterator = c.iterator();
				Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
				T result = iterator.next();
				Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
				return result;
			}
		});

		project.getPluginManager().apply(NativeCompileCapabilityPlugin.class);
		project.getPluginManager().apply(NativeLinkCapabilityPlugin.class);
		project.getPluginManager().apply(NativeArchiveCapabilityPlugin.class);

		components(project).withType(NativeComponentSpec.class)
			.configureEach(new TargetedNativeComponentDimensionsRule(project.getObjects().newInstance(ToolChainSelectorInternal.class)));
		model(project, objects()).configureEach(TargetMachineAwareComponent.class, new TargetMachineConventionRule(project.getProviders()));
		model(project, objects()).configureEach(TargetBuildTypeAwareComponent.class, new TargetBuildTypeConventionRule(project.getProviders()));
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

		project.afterEvaluate(__ -> {
			components(project).configureEach(component -> {
				if (component instanceof TargetMachineAwareComponent) {
					((TargetMachineAwareComponent) component).getTargetMachines().disallowChanges();
				}
				if (component instanceof TargetLinkageAwareComponent) {
					((TargetLinkageAwareComponent) component).getTargetLinkages().disallowChanges();
				}
				if (component instanceof TargetBuildTypeAwareComponent) {
					((TargetBuildTypeAwareComponent) component).getTargetBuildTypes().disallowChanges();
				}
			});
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

		project.afterEvaluate(__ -> {
			model(project, mapOf(Component.class))
				.configureEach(VariantComponentSpec.class, new RegisterVariants<>(model(project, registryOf(Variant.class))));
		});

		// TODO: Should be part of native-application-base plugin
		variants(project).withType(DefaultNativeApplicationVariant.class).configureEach(variant -> {
			variant.getBinaryLifecycleTask().configure(configureBuildGroup());
			variant.getBinaryLifecycleTask().configure(task -> {
				task.setDescription(String.format("Assembles a executable binary containing the objects files of %s.", "executable binary '" + variant.getExecutable().getName() + "'"));
				task.dependsOn((Callable<Object>) variant.getExecutable()::get);
			});
		});

		// TODO: Should be part of native-library-base plugin
		variants(project).withType(DefaultNativeLibraryVariant.class).configureEach(variant -> {
			variant.getBinaryLifecycleTask().configure(configureBuildGroup());
			variant.getBinaryLifecycleTask().configure(task -> {
				if (variant.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).isShared()) {
					task.setDescription(String.format("Assembles a shared library binary containing the objects files of %s.", "shared library binary '" + variant.getSharedOrStaticLibraryBinary().getName() + "'"));
				} else {
					task.setDescription(String.format("Assembles a static library binary containing the objects files of %s.", "static library binary '" + variant.getSharedOrStaticLibraryBinary().getName() + "'"));
				}

				task.dependsOn((Callable<Object>) variant.getSharedOrStaticLibraryBinary()::get);
			});
		});

		model(project, mapOf(Component.class)).configureEach(VariantComponentSpec.class, component -> {
			final VariantView<?> variants = ((VariantAwareComponent<?>) component).getVariants();
			component.getDevelopmentVariant().convention(project.provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) variants.map(VariantInternal.class::cast).get())));
		});
		variants(project).withType(DefaultNativeApplicationVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		variants(project).withType(DefaultNativeApplicationVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		variants(project).withType(DefaultNativeLibraryVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) variant.getDevelopmentBinary()::get));
			}
		});
		variants(project).withType(DefaultNativeLibraryVariant.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getObjectsTask().configure(configureDependsOn(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS.transform(variant)));
			}
		});

		// TODO: Restrict this rule
		components(project).configureEach(component -> {
			if (component instanceof ObjectsTaskMixIn && component instanceof HasDevelopmentVariant) {
				((ObjectsTaskMixIn) component).getObjectsTask().configure(configureDependsOn(((HasDevelopmentVariant<?>) component).getDevelopmentVariant().flatMap(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS)));
			}
		});

		model(project, objects()).configureEach(AbstractLinkTask.class, new BiConsumer<ModelObjects.ModelObjectIdentity, AbstractLinkTask>() {
			@Override
			public void accept(ModelObjects.ModelObjectIdentity identity, AbstractLinkTask task) {
				identity.getParent().filter(this::isNativeLinkableBinary).ifPresent(__ -> {
					// Until we model the build type
					task.getDebuggable().set(false);
				});
			}

			private boolean isNativeLinkableBinary(ModelObjects.ModelObjectIdentity it) {
				return it.instanceOf(NativeBundleBinarySpec.class) || it.instanceOf(NativeExecutableBinarySpec.class) || it.instanceOf(NativeSharedLibraryBinarySpec.class);
			}
		});
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
