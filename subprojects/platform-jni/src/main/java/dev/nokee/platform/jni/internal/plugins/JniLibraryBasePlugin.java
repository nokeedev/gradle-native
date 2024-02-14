/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.jni.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.nokee.language.base.HasCompileTask;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.GroovySourceSetSpec;
import dev.nokee.language.jvm.internal.JavaSourceSetSpec;
import dev.nokee.language.jvm.internal.KotlinSourceSetSpec;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.language.nativebase.NativeSourceSetComponentDependencies;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.internal.KnownModelObject;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInternal;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.jni.JarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.platform.jni.internal.ConfigureJniHeaderDirectoryOnJavaCompileAction;
import dev.nokee.platform.jni.internal.DefaultJavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.internal.DefaultJavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.jni.internal.JniJarBinarySpec;
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
import dev.nokee.platform.jni.internal.JniLibraryInternal;
import dev.nokee.platform.jni.internal.JvmJarBinarySpec;
import dev.nokee.platform.jni.internal.actions.WhenPlugin;
import dev.nokee.platform.nativebase.internal.HasRuntimeLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.RequestFrameworkAction;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.internal.rules.TargetedNativeComponentDimensionsRule;
import dev.nokee.platform.nativebase.internal.services.UnbuildableWarningService;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import dev.nokee.util.provider.ZipProviderBuilder;
import dev.nokee.utils.ConfigurationUtils;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.platform.NativePlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.artifacts;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.platform.jni.internal.actions.WhenPlugin.any;
import static dev.nokee.platform.jni.internal.plugins.JvmIncludeRoots.jvmIncludes;
import static dev.nokee.platform.jni.internal.plugins.NativeCompileTaskProperties.includeRoots;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;
import static dev.nokee.util.ProviderOfIterableTransformer.toProviderOfIterable;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.createIfAbsent;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureDescription;
import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath;
import static dev.nokee.utils.TransformerUtils.onlyElement;
import static dev.nokee.utils.TransformerUtils.transformEach;
import static java.util.Collections.emptyList;

public class JniLibraryBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(LanguageBasePlugin.class);
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(JvmLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		model(project, factoryRegistryOf(Artifact.class)).registerFactory(JniJarBinarySpec.class);
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(JvmJarBinarySpec.class);

//		// FIXME: This is temporary until we convert all entity
//		project.afterEvaluate(__ -> {
//			model(project, mapOf(Variant.class)).whenElementKnown(it -> it.realizeNow()); // Because outgoing configuration are created when variant realize
//		});

		model(project, mapOf(Component.class)).whenElementKnown(JniLibraryComponentInternal.class, knownComponent -> {
			final Configuration apiElements = model(project).getExtensions().getByType(ConfigurationFactory.class).newConsumable(knownComponent.getIdentifier().child("apiElements"));

			ConfigurationUtils.<Configuration>configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_API)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))).execute(apiElements);

			project.getConfigurations().matching(it -> it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(knownComponent.getIdentifier().child("api")).toString())).all(it -> {
				apiElements.extendsFrom(it);
			});
		});

		model(project, mapOf(Component.class)).whenElementFinalized(JniLibraryComponentInternal.class, component -> {
			final Configuration runtimeElements = model(project).getExtensions().getByType(ConfigurationFactory.class).newConsumable(component.getIdentifier().child("runtimeElements"));

			ConfigurationUtils.<Configuration>configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))).execute(runtimeElements);

			project.getConfigurations().matching(it -> it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child("api")).toString()) || it.getName().equals(ModelObjectIdentifiers.asFullyQualifiedName(component.getIdentifier().child("jvmRuntimeOnly")).toString())).all(it -> {
				runtimeElements.extendsFrom(it);
			});

			// Attach Jni JARs
			if (component.getBuildVariants().get().size() > 1 || (!project.getPluginManager().hasPlugin("java") && !project.getPluginManager().hasPlugin("groovy") && !project.getPluginManager().hasPlugin("org.jetbrains.kotlin.jvm"))) {
				val toolChainSelector = project.getObjects().newInstance(ToolChainSelectorInternal.class);
				// TODO: should be able to query KnownModelObject from the ModelMap
				final Set<KnownModelObject<JniLibraryInternal>> allBuildableVariants = model(project, objects()).getElements(JniLibraryInternal.class, obj -> {
					if (obj.getType().isSubtypeOf(JniLibraryInternal.class) && ModelObjectIdentifiers.descendantOf(obj.getIdentifier(), component.getIdentifier())) {
						if (ModelObjectIdentifiers.asFullyQualifiedName(obj.getIdentifier()).toString().contains("*")) {
							return true; // discovering elements
						} else {
							final VariantIdentifier variantIdentifier = (VariantIdentifier) obj.getIdentifier();
							return toolChainSelector.canBuild(((BuildVariantInternal) variantIdentifier.getBuildVariant()).getAxisValue(TARGET_MACHINE_COORDINATE_AXIS));
						}
					}
					return false;
				}).get();

				final ModelObject<Sync> syncTask = model(project, registryOf(Task.class)).register(component.getIdentifier().child(TaskName.of("sync", "jniJars")), Sync.class);
				syncTask.configure(task -> {
					task.setDestinationDir(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task)).get().getAsFile());
				});
				for (KnownModelObject<JniLibraryInternal> buildableVariant : allBuildableVariants) {
					final Provider<String> artifactFileName = component.getBaseName().map(baseName -> baseName + ((VariantIdentifier) buildableVariant.getIdentifier()).getAmbiguousDimensions().getAsKebabCase().map(it -> "-" + it).orElse("")).map(it -> it + ".jar");

					syncTask.configure(task -> {
						task.from(buildableVariant.asProvider().flatMap(it -> it.getJavaNativeInterfaceJar().getJarTask()), spec -> spec.rename(__ -> artifactFileName.get()));
					});

					final Provider<File> artifactFile = ZipProviderBuilder.newBuilder(project.getObjects())
						.value(syncTask.asProvider().map(Sync::getDestinationDir))
						.value(artifactFileName)
						.zip((destinationDirectory, fileName) -> new File(destinationDirectory, fileName));

					runtimeElements.getOutgoing().artifact(artifactFile);
				}
			}

			if ((project.getPluginManager().hasPlugin("java") || project.getPluginManager().hasPlugin("groovy") || project.getPluginManager().hasPlugin("org.jetbrains.kotlin.jvm")) && component.getBuildVariants().get().size() == 1) {
				project.getTasks().named(project.getExtensions().getByType(SourceSetContainer.class).getByName(component.getName()).getJarTaskName(), configureDependsOn((Callable<?>) () -> {
					val toolChainSelector = project.getObjects().newInstance(ToolChainSelectorInternal.class);
					return model(project, objects()).getElements(JniLibraryInternal.class, obj -> {
						if (obj.getType().isSubtypeOf(JniLibraryInternal.class) && ModelObjectIdentifiers.descendantOf(obj.getIdentifier(), component.getIdentifier())) {
							if (ModelObjectIdentifiers.asFullyQualifiedName(obj.getIdentifier()).toString().contains("*")) {
								return true; // discovering elements
							} else {
								final VariantIdentifier variantIdentifier = (VariantIdentifier) obj.getIdentifier();
								return toolChainSelector.canBuild(((BuildVariantInternal) variantIdentifier.getBuildVariant()).getAxisValue(TARGET_MACHINE_COORDINATE_AXIS));
							}
						}
						return false;
					}).map(transformEach(it -> it.asProvider().flatMap(elementsOf(JniLibraryInternal::getNativeRuntimeFiles)))).map(toProviderOfIterable(project.getObjects()::listProperty));
					// TODO: Should be same as bottom configuration
//					return component.getVariants().get().stream().map(it -> it.getNativeRuntimeFiles()).collect(Collectors.toList()); // realize variant
//					return Collections.emptyList();
				}));
			}
		});

		// Attach JVM Jar binary
		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), __ -> {
			model(project, mapOf(Component.class)).whenElementKnown(JniLibraryComponentInternal.class, knownComponent -> {
				// Note: If it's not a main component, then we need to handle outgoing element that would normally be added by JVM plugins
				if (!knownComponent.getIdentifier().getName().equals(ElementName.ofMain())) {
					final Configuration apiElements = createIfAbsent(project.getConfigurations(), ModelObjectIdentifiers.asFullyQualifiedName(knownComponent.getIdentifier().child("apiElements")).toString());
					final Configuration runtimeElements = createIfAbsent(project.getConfigurations(), ModelObjectIdentifiers.asFullyQualifiedName(knownComponent.getIdentifier().child("runtimeElements")).toString());

					final ModelObject<Sync> syncTask = model(project, registryOf(Task.class)).register(knownComponent.getIdentifier().child(TaskName.of("sync", "jvmJar")), Sync.class);
					syncTask.configure(task -> {
						task.from(knownComponent.asProvider().flatMap(it -> it.getBinaries().withType(JvmJarBinary.class).getElements()).map(onlyElement()).flatMap(JarBinary::getJarTask));
						task.setDestinationDir(project.getLayout().getBuildDirectory().dir(temporaryDirectoryPath(task)).get().getAsFile());
					});

					final Provider<File> artifactFile = ZipProviderBuilder.newBuilder(project.getObjects())
						.value(syncTask.asProvider().map(Sync::getDestinationDir))
						.value(knownComponent.asProvider().flatMap(HasBaseName::getBaseName))
						.zip((destinationDirectory, baseName) -> new File(destinationDirectory, baseName + ".jar"));

					apiElements.getOutgoing().artifact(artifactFile);
					runtimeElements.getOutgoing().artifact(artifactFile);
				}
			});
		}).execute(project);

		project.getPluginManager().withPlugin("java", appliedPlugin -> {
			model(project, mapOf(Component.class)).whenElementKnown(JniLibraryComponentInternal.class, knownComponent -> {
				if (knownComponent.getIdentifier().getName().equals(ElementName.ofMain())) {
					project.getConfigurations().getByName("implementation", configureExtendsFrom(createIfAbsent(project.getConfigurations(), "jvmImplementation")));
					project.getConfigurations().getByName("runtimeOnly", configureExtendsFrom(createIfAbsent(project.getConfigurations(), "jvmRuntimeOnly")));
				}
			});
		});

		model(project, mapOf(Component.class)).whenElementKnown(JniLibraryComponentInternal.class, knownComponent -> {
			final Configuration jvmImplementation = createIfAbsent(project.getConfigurations(), ModelObjectIdentifiers.asFullyQualifiedName(knownComponent.getIdentifier().child("jvmImplementation")).toString());
			final Configuration jvmRuntimeOnly = createIfAbsent(project.getConfigurations(), ModelObjectIdentifiers.asFullyQualifiedName(knownComponent.getIdentifier().child("jvmRuntimeOnly")).toString());
			final Configuration api = createIfAbsent(project.getConfigurations(), ModelObjectIdentifiers.asFullyQualifiedName(knownComponent.getIdentifier().child("api")).toString());

			ConfigurationUtils.configureAsDeclarable().execute(jvmImplementation);
			ConfigurationUtils.configureAsDeclarable().execute(jvmRuntimeOnly);

			jvmImplementation.extendsFrom(api);
		});

		components(project).withType(JniLibraryComponentInternal.class)
			.configureEach(new TargetedNativeComponentDimensionsRule(project.getObjects().newInstance(ToolChainSelectorInternal.class)));
		components(project).withType(JniLibraryComponentInternal.class).configureEach(new Action<JniLibraryComponentInternal>() {
			@Override
			public void execute(JniLibraryComponentInternal component) {
				final DefaultJavaNativeInterfaceLibraryComponentDependencies dependencies = component.getDependencies();
				configureFrameworkAwareness(dependencies.getNative());
				extendsFromImplementation(dependencies.getNative());

				// Propagate to variants
				component.getVariants().configureEach(JniLibraryInternal.class, variant -> {
					final DefaultJavaNativeInterfaceNativeComponentDependencies variantDependencies = variant.getDependencies();

					extendsFromParent(variantDependencies.getNative(), dependencies.getNative());
					extendsFromImplementation(variantDependencies.getNative());

					variant.getSources().configureEach(extendsFromParentCompileOnly(variantDependencies.getNative().getCompileOnly()));

					variant.getBinaries().configureEach(withElement((element, binary) -> {
						if (binary instanceof HasLinkLibrariesDependencyBucket) {
							((HasLinkLibrariesDependencyBucket) binary).getLinkLibraries().extendsFrom(variantDependencies.getNativeImplementation(), variantDependencies.getNativeLinkOnly());
						}
						if (binary instanceof HasRuntimeLibrariesDependencyBucket) {
							((HasRuntimeLibrariesDependencyBucket) binary).getRuntimeLibraries().extendsFrom(variantDependencies.getNativeImplementation(), variantDependencies.getNativeRuntimeOnly());
						}
					}));
				});

//			project.getPlugins().withType(NativeLanguagePlugin.class, new OnceAction<>(appliedPlugin -> {
//				// TODO: configure child headerSearchPaths to extends from nativeCompileOnly
//			}));
			}

			private void configureFrameworkAwareness(DefaultNativeComponentDependencies dependencies) {
				dependencies.getCompileOnly().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));
				dependencies.getImplementation().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));
				dependencies.getLinkOnly().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));
				dependencies.getRuntimeOnly().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));
			}

			private void extendsFromParent(DefaultNativeComponentDependencies variantDependencies, DefaultNativeComponentDependencies parentDependencies) {
				variantDependencies.getImplementation().extendsFrom(parentDependencies.getImplementation());
				variantDependencies.getCompileOnly().extendsFrom(parentDependencies.getCompileOnly());
				variantDependencies.getLinkOnly().extendsFrom(parentDependencies.getLinkOnly());
				variantDependencies.getRuntimeOnly().extendsFrom(parentDependencies.getRuntimeOnly());
			}

			private void extendsFromImplementation(DefaultNativeComponentDependencies dependencies) {
				dependencies.getCompileOnly().extendsFrom(dependencies.getImplementation());
				dependencies.getLinkOnly().extendsFrom(dependencies.getImplementation());
				dependencies.getRuntimeOnly().extendsFrom(dependencies.getImplementation());
			}

			private Action<LanguageSourceSet> extendsFromParentCompileOnly(DependencyBucket parentCompileOnly) {
				return sourceSet -> {
					if (sourceSet instanceof HasHeaderSearchPaths) {
						((DependencyBucketInternal) ((DependencyAwareComponent<NativeSourceSetComponentDependencies>) sourceSet).getDependencies().getCompileOnly()).extendsFrom(parentCompileOnly);
					}
				};
			}
		});

		// Component rules
		project.getPluginManager().withPlugin("java", ignored -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				component.getSources().configureEach(JavaSourceSetSpec.class, sourceSet -> {
					sourceSet.getCompileTask().configure(new ConfigureJniHeaderDirectoryOnJavaCompileAction(component.getIdentifier(), project.getLayout()));
				});
				component.getSources().configureEach(HasHeaders.class, sourceSet -> {
					sourceSet.getHeaders().from((Callable<Object>) component.getSources().withType(JavaSourceSetSpec.class).getElements().map(it -> {
						final ConfigurableFileCollection result = project.getObjects().fileCollection();
						for (JavaSourceSetSpec spec : it) {
							result.from(spec.getCompileTask().flatMap(t -> t.getOptions().getHeaderOutputDirectory()));
						}
						return result;
					})::get);
				});
			});
		});

		// TODO: This is an external dependency meaning we should go through the component dependencies.
		//  We can either add an file dependency or use the, yet-to-be-implemented, shim to consume system libraries
		//  We aren't using a language source set as the files will be included inside the IDE projects which is not what we want.
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getSources().configureEach(HasCompileTask.class, sourceSet -> {
				sourceSet.getCompileTask().configure(includeRoots(from(jvmIncludes())));
			});
		});

		project.getPluginManager().withPlugin("groovy", ignored -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				model(project, registryOf(LanguageSourceSet.class)).register(component.getIdentifier().child("groovy"), GroovySourceSetSpec.class).get(); // force realize to avoid out-of-order
			});
		});
		project.getPluginManager().withPlugin("java", ignored -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				model(project, registryOf(LanguageSourceSet.class)).register(component.getIdentifier().child("java"), JavaSourceSetSpec.class).get(); // force realize to avoid out-of-order
			});
		});
		project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", ignored -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				model(project, registryOf(LanguageSourceSet.class)).register(component.getIdentifier().child("kotlin"), KotlinSourceSetSpec.class).get(); // force realize to avoid out-of-order
			});
		});
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			Provider<? extends List<? extends JniLibrary>> allBuildableVariants = component.getVariants().filter(v -> v.getSharedLibrary().isBuildable());
			component.getAssembleTask().configure(configureDependsOn(component.getDevelopmentVariant().map(JniLibrary::getJavaNativeInterfaceJar).map(Collections::singletonList).orElse(Collections.emptyList())));
			component.getAssembleTask().configure(task -> {
				task.dependsOn((Callable<Object>) () -> {
					val buildVariants = component.getBuildVariants().get();
					val firstBuildVariant = Iterables.getFirst(buildVariants, null);
					if (buildVariants.size() == 1 && allBuildableVariants.get().isEmpty() && firstBuildVariant.hasAxisOf(TargetMachines.host().getOperatingSystemFamily())) {
						throw new RuntimeException(String.format("No tool chain is available to build for platform '%s'", platformNameFor(((BuildVariantInternal) firstBuildVariant).getAxisValue(TARGET_MACHINE_COORDINATE_AXIS))));
					}
					return ImmutableList.of();
				});
			});
		});

		// TODO: We should discover the variant instead of gating them
		project.afterEvaluate(__ -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				// See https://github.com/nokeedev/gradle-native/issues/543
				if (component.getBuildVariants().get().size() > 1) {
					component.getVariants().configureEach(JniLibraryInternal.class, it -> {
						it.getJavaNativeInterfaceJar().getJarTask().configure(task -> {
							task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("libs/" + component.getIdentifier().getName()));
						});
					});
				}
			});
		});
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(JvmJarBinarySpec.class, binary -> {
				binary.getJarTask().configure(configureDependsOn((Callable<Object>) component.getSources().withType(JavaSourceSetSpec.class).map(it -> it.getCompileTask().get())::get));
			});
		});
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(JvmJarBinarySpec.class, binary -> {
				binary.getJarTask().configure(configureDependsOn((Callable<Object>) component.getSources().withType(GroovySourceSetSpec.class).map(it -> it.getCompileTask().get())::get));
			});
		});
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(JvmJarBinarySpec.class, binary -> {
				binary.getJarTask().configure(configureDependsOn((Callable<Object>) component.getSources().withType(KotlinSourceSetSpec.class).map(it -> it.getCompileTask().get())::get));
			});
		});

		artifacts(project).withType(JniJarBinarySpec.class).configureEach(binary -> {
			binary.getJarTask().configure(configureBuildGroup());
			binary.getJarTask().configure(task -> {
				task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("libs"));
			});
			binary.getJarTask().configure(task -> {
				task.getArchiveBaseName().convention(binary.getIdentifier().getName().toString());
			});
			binary.getJarTask().configure(task -> {
				if (task.getDescription() == null) {
					task.setDescription(String.format("Assembles a JAR archive containing the shared library for %s.", binary));
				}
			});
		});

		artifacts(project).withType(JvmJarBinarySpec.class).configureEach(binary -> {
			binary.getJarTask().configure(configureBuildGroup());
			binary.getJarTask().configure(task -> {
				task.getDestinationDirectory().convention(project.getLayout().getBuildDirectory().dir("libs"));
			});
			binary.getJarTask().configure(task -> {
				task.getArchiveBaseName().convention(binary.getIdentifier().getName().toString());
			});
			binary.getJarTask().configure(task -> {
				if (task.getDescription() == null) {
					task.setDescription(String.format("Assembles a JAR archive containing the classes for %s.", binary));
				}
			});
		});


		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(JvmJarBinarySpec.class, binary -> {
				binary.getJarTask().configure(task -> {
					if (component.getBuildVariants().get().size() == 1) {
						task.setDescription(String.format("Assembles a JAR archive containing the classes and shared library for %s.", binary));
					}
				});
			});
		});

		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(JvmJarBinarySpec.class, binary -> {
				binary.getJarTask().configure(task -> task.getArchiveBaseName().set(component.getBaseName()));
			});
		});
		val registerJvmJarBinaryAction = new Action<AppliedPlugin>() {
			@Override
			public void execute(AppliedPlugin ignored) {
				components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
					model(project, registryOf(Artifact.class)).register(component.getIdentifier().child(ElementName.ofMain("jvmJar")), JvmJarBinarySpec.class);
				});
			}
		};
		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), registerJvmJarBinaryAction).execute(project);

		// Assemble task configuration
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getAssembleTask().configure(configureDependsOn((Callable<Object>) component.getBinaries().filter(it -> it instanceof JvmJarBinarySpec)::get));
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			if (!variant.getIdentifier().getUnambiguousName().isEmpty()) {
				variant.getAssembleTask().configure(configureDependsOn(variant.getJavaNativeInterfaceJar()));
			}
		});

		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), ignored -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				component.getVariants().configureEach(JniLibraryInternal.class, variant -> {
					variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) component.getBinaries().filter(it -> it instanceof JvmJarBinarySpec)::get));
				});
			});
		}).execute(project);

		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getTargetLinkages().convention(Collections.singletonList(TargetLinkages.SHARED));
		});

		// Variant rules
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getResourcePath().convention(variant.getIdentifier().getAmbiguousDimensions().getAsKebabCase().orElse(""));
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getSharedLibraryTask().configure(configureBuildGroup());
			variant.getSharedLibraryTask().configure(configureDescription("Assembles the shared library binary of %s.", variant));
			variant.getSharedLibraryTask().configure(configureDependsOn(variant.getSharedLibrary()));
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.sharedLibrary(binary -> {
				binary.getCompileTasks().configureEach(configureTargetPlatform(set(fromBuildVariant(variant.getIdentifier().getBuildVariant()))));
			});
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getObjectsTask().configure(configureDependsOn(variant.getSharedLibrary().getCompileTasks().filter(it -> it instanceof HasObjectFiles)));
			variant.getObjectsTask().configure(configureBuildGroup());
			variant.getObjectsTask().configure(configureDescription("Assembles the object files of %s.", variant));
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getNativeRuntimeFiles().from(variant.getSharedLibrary().getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile));
			variant.getNativeRuntimeFiles().from((Callable<Object>) () -> variant.getSharedLibrary().getRuntimeLibraries().getAsConfiguration().getIncoming().getFiles());
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getJavaNativeInterfaceJar());
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getBinaries().configureEach(JniJarBinarySpec.class, binary -> {
				binary.getJarTask().configure(task -> task.getArchiveBaseName()
					.set(variant.getBaseName().map(baseName -> baseName + variant.getIdentifier().getAmbiguousDimensions().getAsKebabCase().map(it -> "-" + it).orElse(""))));
			});
		});

		val unbuildableWarningService = (Provider<UnbuildableWarningService>) project.getGradle().getSharedServices().getRegistrations().getByName(UnbuildableWarningService.class.getSimpleName()).getService();

		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getVariants().configureEach(JniLibraryInternal.class, variant -> {
				variant.getJavaNativeInterfaceJar().getJarTask().configure(configureJarTaskUsing(project.provider(() -> variant), unbuildableWarningService.map(it -> {
					it.warn(component.getIdentifier());
					return null;
				})));
			});
		});
	}

	// NOTE(daniel): I added the diagnostic because I lost about 2 hours debugging missing files from the generated JAR file.
	//  The concept of diagnostic is something I want to push forward throughout Nokee to inform the user of possible configuration problems.
	//  I'm still hesitant at this stage to throw an exception vs warning in the console.
	//  Some of the concept here should be shared with the incompatible plugin usage (and vice-versa).
	private static class MissingFileDiagnostic {
		private boolean hasAlreadyRan = false;
		private final List<File> missingFiles = new ArrayList<>();

		public void logTo(Logger logger) {
			if (!missingFiles.isEmpty()) {
				StringBuilder builder = new StringBuilder();
				builder.append("The following file");
				if (missingFiles.size() > 1) {
					builder.append("s are");
				} else {
					builder.append(" is");
				}
				builder.append(" missing and will be absent from the JAR file:").append(System.lineSeparator());
				for (File file : missingFiles) {
					builder.append(" * ").append(file.getPath()).append(System.lineSeparator());
				}
				builder.append("We recommend taking the following actions:").append(System.lineSeparator());
				builder.append(" - Verify 'nativeRuntimeFile' property configuration for each variants").append(System.lineSeparator());
				builder.append("Missing files from the JAR file can lead to runtime errors such as 'NoClassDefFoundError'.");
				logger.warn(builder.toString());
			}
		}

		public void missingFiles(List<File> missingFiles) {
			this.missingFiles.addAll(missingFiles);
		}

		public void run(Consumer<MissingFileDiagnostic> action) {
			if (!hasAlreadyRan) {
				action.accept(this);
				hasAlreadyRan = false;
			}
		}
	}

	private static Action<Jar> configureJarTaskUsing(Provider<JniLibrary> library, Provider<Void> logger) {
		return task -> {
			MissingFileDiagnostic diagnostic = new MissingFileDiagnostic();
			task.doFirst(new Action<Task>() {
				@Override
				public void execute(Task task) {
					diagnostic.run(warnAboutMissingFiles(task.getInputs().getSourceFiles()));
					diagnostic.logTo(task.getLogger());
				}

				private Consumer<MissingFileDiagnostic> warnAboutMissingFiles(Iterable<File> files) {
					return diagnostic -> {
						ImmutableList.Builder<File> builder = ImmutableList.builder();
						File linkedFile = library.map(it -> it.getSharedLibrary().getLinkTask().get().getLinkedFile().get().getAsFile()).get();
						for (File file : files) {
							if (!file.exists() && !file.equals(linkedFile)) {
								builder.add(file);
							}
						}
						diagnostic.missingFiles(builder.build());
					};
				}
			});
			task.from(library.flatMap(it -> {
				// TODO: The following is debt that we accumulated from gradle/gradle.
				//  The real condition to check is, do we know of a way to build the target machine on the current host.
				//  If yes, we crash the build by attaching the native file which will tell the user how to install the right tools.
				//  If no, we can "silently" ignore the build by saying you can't build on this machine.
				//  One consideration is to deactivate publishing so we don't publish a half built jar.
				//  TL;DR:
				//    - Single variant where no toolchain could ever build the binary (unavailable) => print a warning
				//    - Single variant where no toolchain is found to build the binary (unbuildable) => fail
				//    - Single variant where toolchain is found to build the binary (buildable) => build (and hopefully succeed)
				if (task.getName().equals("jar")) {
					// TODO: Test this scenario in a functional test
					if (it.getSharedLibrary().isBuildable()) {
						return it.getNativeRuntimeFiles().getElements();
					} else {
						logger.getOrNull();
						return ProviderUtils.fixed(emptyList());
					}
				}
				return it.getNativeRuntimeFiles().getElements();
			}), spec -> {
				// Don't resolve the resourcePath now as the JVM Kotlin plugin (as of 1.3.72) was resolving the `jar` task early.
				spec.into(library.map(JniLibrary::getResourcePath));
			});
		};
	}

	//region Target platform
	public static <SELF extends Task> Action<SELF> configureTargetPlatform(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends NativePlatform>> action) {
		return task -> {
			action.accept(task, wrap(targetPlatformProperty(task)));
		};
	}

	private static NativePlatform fromBuildVariant(BuildVariant buildVariant) {
		return NativePlatformFactory.create(buildVariant).get();
	}

	private static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof AbstractNativeCompileTask) {
			return ((AbstractNativeCompileTask) task).getTargetPlatform();
		} else if (task instanceof SwiftCompile) {
			return ((SwiftCompile) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
