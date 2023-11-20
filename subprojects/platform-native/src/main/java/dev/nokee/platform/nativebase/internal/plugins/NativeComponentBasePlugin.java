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
import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.tasks.SourceCompile;
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
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasApiDependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.TaskViewAdapter;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTask;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInternal;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.rules.ExtendsFromImplementationDependencyBucketAction;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.AttachAttributesToConfigurationRule;
import dev.nokee.platform.nativebase.internal.BundleBinaryInternal;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationVariant;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryVariant;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.HasRuntimeLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.NativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.NativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.NativeVariant;
import dev.nokee.platform.nativebase.internal.ObjectsTaskMixIn;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfigurationRegistrationRule;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.StaticLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.TargetBuildTypesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.TargetLinkagesPropertyRegistrationRule;
import dev.nokee.platform.nativebase.internal.TargetMachinesPropertyRegistrationRule;
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
import dev.nokee.platform.nativebase.internal.rules.ToBinariesCompileTasksTransformer;
import dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer;
import dev.nokee.platform.nativebase.internal.services.UnbuildableWarningService;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sources;
import static dev.nokee.model.internal.ModelElementSupport.safeAsModelElement;
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

		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new dev.nokee.platform.base.internal.rules.ExtendsFromParentDependencyBucketAction<LinkOnlyDependencyBucketMixIn>(model(project, objects())) {
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

		final Factory<TaskView<SourceCompile>> compileTasksFactory = () -> {
			Task.Namer namer = new Task.Namer();
			ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
			Runnable realizeNow = () -> {
				model(project, mapOf(LanguageSourceSet.class)).whenElementKnow(it -> {
					if (ModelObjectIdentifiers.descendantOf(it.getIdentifier(), identifier)) {
						it.realizeNow(); // force realize
					}
				});
			};
			return new TaskViewAdapter<>(new ViewAdapter<>(SourceCompile.class, new ModelNodeBackedViewStrategy(it -> namer.determineName((Task) it), project.getTasks(), project.getProviders(), project.getObjects(), realizeNow, identifier)));
		};
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(SharedLibraryBinaryInternal.class, name -> {
			return project.getObjects().newInstance(SharedLibraryBinaryInternal.class, model(project, registryOf(Task.class)), model(project, registryOf(DependencyBucket.class)), compileTasksFactory);
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(BundleBinaryInternal.class, name -> {
			return project.getObjects().newInstance(BundleBinaryInternal.class, model(project, registryOf(Task.class)), model(project, registryOf(DependencyBucket.class)), compileTasksFactory);
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(ExecutableBinaryInternal.class, name -> {
			return project.getObjects().newInstance(ExecutableBinaryInternal.class, model(project, registryOf(Task.class)), model(project, registryOf(DependencyBucket.class)), compileTasksFactory);
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(StaticLibraryBinaryInternal.class, name -> {
			return project.getObjects().newInstance(StaticLibraryBinaryInternal.class, model(project, registryOf(Task.class)), compileTasksFactory);
		});
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeApplicationVariant.class, name -> {
			return project.getObjects().newInstance(DefaultNativeApplicationVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}));
		});
		variants(project).withType(DefaultNativeApplicationVariant.class).configureEach(result -> {
			result.getDevelopmentBinary().convention(result.getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(result.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
		});
		model(project, factoryRegistryOf(Variant.class)).registerFactory(DefaultNativeLibraryVariant.class, name -> {
			return project.getObjects().newInstance(DefaultNativeLibraryVariant.class, model(project, registryOf(DependencyBucket.class)), model(project, registryOf(Task.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<SourceView<LanguageSourceSet>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}));
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
					((ExtensionAware) variant).getExtensions().add("executable", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("executable")), ExecutableBinaryInternal.class).asProvider());
				} else if (linkage.isShared()) {
					((ExtensionAware) variant).getExtensions().add("sharedLibrary", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("sharedLibrary")), SharedLibraryBinaryInternal.class).asProvider());
				} else if (linkage.isBundle()) {
					((ExtensionAware) variant).getExtensions().add("bundle", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("bundle")), BundleBinaryInternal.class).asProvider());
				} else if (linkage.isStatic()) {
					((ExtensionAware) variant).getExtensions().add("staticLibrary", model(project, registryOf(Artifact.class)).register(identifier.child(ElementName.ofMain("staticLibrary")), StaticLibraryBinaryInternal.class).asProvider());
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

		components(project).configureEach(new TargetMachinesPropertyRegistrationRule(project.getObjects().newInstance(ToolChainSelectorInternal.class)));
		components(project).configureEach(component -> {
			if (component instanceof TargetMachineAwareComponent) {
				((TargetMachineAwareComponent) component).getTargetMachines().convention(singletonList(TargetMachines.host()));
			}
		});
		components(project).configureEach(new TargetBuildTypesPropertyRegistrationRule());
		components(project).configureEach(component -> {
			if (component instanceof TargetBuildTypeAwareComponent) {
				((TargetBuildTypeAwareComponent) component).getTargetBuildTypes().convention(singletonList(TargetBuildTypes.DEFAULT));
			}
		});
		components(project).configureEach(new TargetLinkagesPropertyRegistrationRule());
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
			components(project).configureEach(component -> {
				if (component instanceof NativeApplicationComponent && component instanceof VariantAwareComponent) {
					for (BuildVariant it : ((VariantAwareComponent<?>) component).getBuildVariants().get()) {
						final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
						final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(((dev.nokee.model.internal.ModelElement) component).getIdentifier()).build();
						model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultNativeApplicationVariant.class);
					}
				}
			});
			components(project).configureEach(component -> {
				if (component instanceof NativeLibraryComponent && component instanceof VariantAwareComponent) {
					for (BuildVariant it : ((VariantAwareComponent<?>) component).getBuildVariants().get()) {
						final BuildVariantInternal buildVariant = (BuildVariantInternal) it;
						final VariantIdentifier variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(((dev.nokee.model.internal.ModelElement) component).getIdentifier()).build();
						model(project, registryOf(Variant.class)).register(variantIdentifier, DefaultNativeLibraryVariant.class);
					}
				}
			});
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

		components(project).configureEach(component -> {
			if (component instanceof NativeApplicationComponent && component instanceof HasDevelopmentVariant && component instanceof VariantAwareComponent) {
				final VariantView<?> variants = ((VariantAwareComponent<?>) component).getVariants();
				((HasDevelopmentVariant<DefaultNativeApplicationVariant>) component).getDevelopmentVariant().convention((Provider<? extends DefaultNativeApplicationVariant>) project.provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) variants.map(VariantInternal.class::cast).get())));
			}
		});
		components(project).configureEach(component -> {
			if (component instanceof NativeLibraryComponent && component instanceof HasDevelopmentVariant && component instanceof VariantAwareComponent) {
				final VariantView<?> variants = ((VariantAwareComponent<?>) component).getVariants();
				((HasDevelopmentVariant<DefaultNativeLibraryVariant>) component).getDevelopmentVariant().convention((Provider<? extends DefaultNativeLibraryVariant>) project.provider(new BuildableDevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) variants.map(VariantInternal.class::cast).get())));
			}
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
	}
}
