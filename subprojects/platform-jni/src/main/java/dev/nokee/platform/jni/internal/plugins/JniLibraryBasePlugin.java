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
import com.google.common.collect.Streams;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.HasCompileTask;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.c.internal.plugins.SupportCSourceSetTag;
import dev.nokee.language.cpp.internal.plugins.SupportCppSourceSetTag;
import dev.nokee.language.jvm.internal.CompileTaskComponent;
import dev.nokee.language.jvm.internal.GroovyLanguageSourceSetComponent;
import dev.nokee.language.jvm.internal.GroovySourceSetSpec;
import dev.nokee.language.jvm.internal.JavaLanguageSourceSetComponent;
import dev.nokee.language.jvm.internal.JavaSourceSetSpec;
import dev.nokee.language.jvm.internal.JvmSourceSetTag;
import dev.nokee.language.jvm.internal.KotlinLanguageSourceSetComponent;
import dev.nokee.language.jvm.internal.KotlinSourceSetSpec;
import dev.nokee.language.jvm.internal.SourceSetComponent;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.language.nativebase.internal.HasHeaderSearchPaths;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.language.objectivecpp.internal.plugins.SupportObjectiveCppSourceSetTag;
import dev.nokee.model.capabilities.variants.IsVariant;
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DependencyBuckets;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.platform.jni.internal.ConfigureJniHeaderDirectoryOnJavaCompileAction;
import dev.nokee.platform.jni.internal.DefaultJavaNativeInterfaceLibraryComponentDependencies;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryVariantRegistrationFactory;
import dev.nokee.platform.jni.internal.JniJarArtifactComponent;
import dev.nokee.platform.jni.internal.JniJarArtifactTag;
import dev.nokee.platform.jni.internal.JniJarBinaryRegistrationFactory;
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
import dev.nokee.platform.jni.internal.JniLibraryInternal;
import dev.nokee.platform.jni.internal.JvmJarArtifactComponent;
import dev.nokee.platform.jni.internal.JvmJarBinaryRegistrationFactory;
import dev.nokee.platform.jni.internal.ModelBackedJniJarBinary;
import dev.nokee.platform.jni.internal.ModelBackedJvmJarBinary;
import dev.nokee.platform.jni.internal.actions.WhenPlugin;
import dev.nokee.platform.nativebase.internal.HasRuntimeLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.RequestFrameworkAction;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.services.UnbuildableWarningService;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import dev.nokee.util.internal.LazyPublishArtifact;
import dev.nokee.utils.ConfigurationUtils;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.platform.NativePlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.platformNameFor;
import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
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
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureDescription;
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

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(GroovySourceSetSpec.class, new ModelObjectFactory<GroovySourceSetSpec>(project, IsLanguageSourceSet.class) {
			@Override
			protected GroovySourceSetSpec doCreate(String name) {
				return project.getObjects().newInstance(GroovySourceSetSpec.class, project.getExtensions().getByType(SourceSetContainer.class));
			}
		});
		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(JavaSourceSetSpec.class, new ModelObjectFactory<JavaSourceSetSpec>(project, IsLanguageSourceSet.class) {
			@Override
			protected JavaSourceSetSpec doCreate(String name) {
				return project.getObjects().newInstance(JavaSourceSetSpec.class, project.getExtensions().getByType(SourceSetContainer.class));
			}
		});
		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(KotlinSourceSetSpec.class, new ModelObjectFactory<KotlinSourceSetSpec>(project, IsLanguageSourceSet.class) {
			@Override
			protected KotlinSourceSetSpec doCreate(String name) {
				return project.getObjects().newInstance(KotlinSourceSetSpec.class, project.getExtensions().getByType(SourceSetContainer.class));
			}
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(ModelBackedJniJarBinary.class, new ModelObjectFactory<ModelBackedJniJarBinary>(project, IsBinary.class) {
			@Override
			protected ModelBackedJniJarBinary doCreate(String name) {
				return project.getObjects().newInstance(ModelBackedJniJarBinary.class, model(project, registryOf(Task.class)));
			}
		});
		model(project, factoryRegistryOf(Artifact.class)).registerFactory(ModelBackedJvmJarBinary.class, new ModelObjectFactory<ModelBackedJvmJarBinary>(project, IsBinary.class) {
			@Override
			protected ModelBackedJvmJarBinary doCreate(String name) {
				return project.getObjects().newInstance(ModelBackedJvmJarBinary.class, model(project, registryOf(Task.class)));
			}
		});

		project.getExtensions().add("__nokee_jniJarBinaryFactory", new JniJarBinaryRegistrationFactory());
		project.getExtensions().add("__nokee_jvmJarBinaryFactory", new JvmJarBinaryRegistrationFactory());
		project.getExtensions().add("__nokee_jniLibraryComponentFactory", new JavaNativeInterfaceLibraryComponentRegistrationFactory());
		project.getExtensions().add("__nokee_jniLibraryVariantFactory", new JavaNativeInterfaceLibraryVariantRegistrationFactory());

		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			final DefaultJavaNativeInterfaceLibraryComponentDependencies dependencies = component.getDependencies();
			dependencies.getNative().getCompileOnly().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));
			dependencies.getNativeImplementation().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));
			dependencies.getNativeLinkOnly().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));
			dependencies.getNativeRuntimeOnly().getDefaultDependencyAction().set(new RequestFrameworkAction(project.getObjects()));

			// Propagate to variants
			component.getVariants().configureEach(variant -> {
				((DeclarableDependencyBucketSpec) variant.getDependencies().getNativeImplementation()).extendsFrom(dependencies.getNativeImplementation());
				((DeclarableDependencyBucketSpec) variant.getDependencies().getNativeLinkOnly()).extendsFrom(dependencies.getNativeLinkOnly());
				((DeclarableDependencyBucketSpec) variant.getDependencies().getNativeRuntimeOnly()).extendsFrom(dependencies.getNativeRuntimeOnly());

				variant.getSources().configureEach(sourceSet -> {
					if (sourceSet instanceof HasHeaderSearchPaths) {
						((HasHeaderSearchPaths) sourceSet).getHeaderSearchPaths().extendsFrom(variant.getDependencies().getNativeImplementation());
					}
				});

				variant.getBinaries().configureEach(binary -> {
					ModelElementSupport.safeAsModelElement(binary).ifPresent(element -> {
						if (binary instanceof HasLinkLibrariesDependencyBucket) {
							((HasLinkLibrariesDependencyBucket) binary).getLinkLibraries().extendsFrom(variant.getDependencies().getNativeImplementation(), variant.getDependencies().getNativeLinkOnly());
						}
						if (binary instanceof HasRuntimeLibrariesDependencyBucket) {
							((HasRuntimeLibrariesDependencyBucket) binary).getRuntimeLibraries().extendsFrom(variant.getDependencies().getNativeImplementation(), variant.getDependencies().getNativeRuntimeOnly());
						}
					});
				});
			});

			dependencies.getJvmImplementation().extendsFrom(dependencies.getApi());

			dependencies.getApiElements().extendsFrom(dependencies.getApi());
			{
				final ConsumableDependencyBucketSpec bucket = dependencies.getApiElements();
				ConfigurationUtils.<Configuration>configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_API)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))).execute(bucket.getAsConfiguration());

				component.getBinaries().configureEach(JvmJarBinary.class, binary -> {
					bucket.getAsConfiguration().getOutgoing().artifact(binary.getJarTask());
				});
			}

			dependencies.getRuntimeElements().extendsFrom(dependencies.getApi());
			{
				final ConsumableDependencyBucketSpec bucket = dependencies.getRuntimeElements();
				ConfigurationUtils.<Configuration>configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))).execute(bucket.getAsConfiguration());

				// Attach JvmJarBinary
				component.getBinaries().configureEach(JvmJarBinary.class, binary -> {
					bucket.getAsConfiguration().getOutgoing().artifact(binary.getJarTask());
				});

				// Attach buildable JniJarBinary
				{
					val toolChainSelector = project.getObjects().newInstance(ToolChainSelectorInternal.class);
					val values = project.getObjects().listProperty(PublishArtifact.class);
					Provider<List<JniLibrary>> allBuildableVariants = component.getVariants().filter(v -> toolChainSelector.canBuild(v.getTargetMachine()));
					Provider<Iterable<JniJarBinary>> allJniJars = allBuildableVariants.map(transformEach(v -> v.getJavaNativeInterfaceJar()));
					val allArtifacts = project.getObjects().listProperty(PublishArtifact.class);
					allArtifacts.set(allJniJars.flatMap(binaries -> {
						val result = project.getObjects().listProperty(PublishArtifact.class);
						for (JniJarBinary binary : binaries) {
							result.add(new LazyPublishArtifact(binary.getJarTask()));
						}
						return result;
					}));
					allArtifacts.finalizeValueOnRead();
					values.addAll(allArtifacts);
					bucket.getAsConfiguration().getOutgoing().getArtifacts().addAllLater(values);
				}
			}

//			project.getPlugins().withType(NativeLanguagePlugin.class, new OnceAction<>(appliedPlugin -> {
//				// TODO: configure child headerSearchPaths to extends from nativeCompileOnly
//			}));

			project.getPluginManager().withPlugin("java", appliedPlugin -> {
				project.getConfigurations().getByName("implementation", configureExtendsFrom(dependencies.getJvmImplementation().getAsConfiguration()));
				project.getConfigurations().getByName("runtimeOnly", configureExtendsFrom(dependencies.getJvmRuntimeOnly().getAsConfiguration()));
			});
		});

		// Component rules
		project.getPluginManager().withPlugin("java", ignored -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				component.getSources().configureEach(JavaSourceSetSpec.class, sourceSet -> {
					sourceSet.getCompileTask().configure(new ConfigureJniHeaderDirectoryOnJavaCompileAction(component.getIdentifier(), project.getLayout()));
				});
				component.getSources().configureEach(sourceSet -> {
					if (sourceSet instanceof HasHeaders) {
						((HasHeaders) sourceSet).getHeaders().from((Callable<Object>) component.getSources().withType(JavaSourceSetSpec.class).getElements().map(it -> {
							final ConfigurableFileCollection result = project.getObjects().fileCollection();
							for (JavaSourceSetSpec spec : it) {
								result.from(spec.getCompileTask().flatMap(t -> t.getOptions().getHeaderOutputDirectory()));
							}
							return result;
						})::get);
					}
				});
			});
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.ofProjection(JniLibraryComponentInternal.class), (entity, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			// TODO: This is an external dependency meaning we should go through the component dependencies.
			//  We can either add an file dependency or use the, yet-to-be-implemented, shim to consume system libraries
			//  We aren't using a language source set as the files will be included inside the IDE projects which is not what we want.
			variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
				variant.getSources().configureEach(sourceSet -> {
					if (sourceSet instanceof HasCompileTask) {
						((HasCompileTask) sourceSet).getCompileTask().configure(includeRoots(from(jvmIncludes())));
					}
				});
			});

			project.getPluginManager().withPlugin("groovy", ignored -> {
				val sourceSet = registry.register(newEntity(identifier.get().child("groovy"), GroovySourceSetSpec.class, it -> it.ownedBy(entity)));
				entity.addComponent(new GroovyLanguageSourceSetComponent(ModelNodes.of(sourceSet)));
			});
			project.getPluginManager().withPlugin("java", ignored -> {
				val sourceSet = registry.register(newEntity(identifier.get().child("java"), JavaSourceSetSpec.class, it -> it.ownedBy(entity)));
				entity.addComponent(new JavaLanguageSourceSetComponent(ModelNodes.of(sourceSet)));
			});
			project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", ignored -> {
				val sourceSet = registry.register(newEntity(identifier.get().child("kotlin"), KotlinSourceSetSpec.class, it -> it.ownedBy(entity)));
				entity.addComponent(new KotlinLanguageSourceSetComponent(ModelNodes.of(sourceSet)));
			});
		})));
		// TODO: When discovery will be a real feature, we shouldn't need this anymore
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			final ModelNode entity = component.getNode();
			project.getPluginManager().withPlugin("dev.nokee.c-language", __ -> entity.addComponentTag(SupportCSourceSetTag.class));
			project.getPluginManager().withPlugin("dev.nokee.cpp-language", __ -> entity.addComponentTag(SupportCppSourceSetTag.class));
			project.getPluginManager().withPlugin("dev.nokee.objective-c-language", __ -> entity.addComponentTag(SupportObjectiveCSourceSetTag.class));
			project.getPluginManager().withPlugin("dev.nokee.objective-cpp-language", __ -> entity.addComponentTag(SupportObjectiveCppSourceSetTag.class));
		});
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getDevelopmentVariant().convention((Provider<? extends JniLibrary>) project.provider(new BuildableDevelopmentVariantConvention(() -> component.getVariants().getElements().get())));
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(JvmSourceSetTag.class), ModelComponentReference.of(SourceSetComponent.class), ModelComponentReference.of(CompileTaskComponent.class), (entity, ignored1, sourceSet, compileTask) -> {
			sourceSet.get().configure(it -> {
				project.getExtensions().getByType(ModelRegistry.class).instantiate(configure(compileTask.get().getId(), Task.class, configureDependsOn((Callable<?>) () -> DependencyBuckets.finalize(project.getConfigurations().getByName(it.getCompileClasspathConfigurationName())))));
			});
		}));
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			Provider<List<JniLibrary>> allBuildableVariants = component.getVariants().filter(v -> v.getSharedLibrary().isBuildable());
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
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ElementNameComponent.class), ModelComponentReference.ofProjection(JniLibraryComponentInternal.class), ModelComponentReference.of(LinkedVariantsComponent.class), (entity, identifier, elementName, projection, variants) -> {
			val variantFactory = new JavaNativeInterfaceLibraryVariantRegistrationFactory();
			val component = ModelNodeUtils.get(entity, JniLibraryComponentInternal.class);

			Streams.zip(component.getBuildVariants().get().stream(), Streams.stream(variants), (buildVariant, variant) -> {
				val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();
				variantFactory.create(variantIdentifier).getComponents().forEach(variant::addComponent);
				ModelStates.register(variant);

				// See https://github.com/nokeedev/gradle-native/issues/543
				if (component.getBuildVariants().get().size() > 1) {
					project.getExtensions().getByType(ModelRegistry.class).instantiate(configure(variant.getId(), JniLibrary.class, it -> {
						it.getJavaNativeInterfaceJar().getJarTask().configure(task -> {
							task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("libs/" + elementName.get()));
						});
					}));
				}

				return null;
			}).forEach(it -> {});
		}));
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(ModelBackedJvmJarBinary.class, binary -> {
				binary.getJarTask().configure(configureDependsOn((Callable<Object>) component.getSources().withType(JavaSourceSetSpec.class).map(it -> it.getCompileTask().get())::get));
			});
		});
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(ModelBackedJvmJarBinary.class, binary -> {
				binary.getJarTask().configure(configureDependsOn((Callable<Object>) component.getSources().withType(GroovySourceSetSpec.class).map(it -> it.getCompileTask().get())::get));
			});
		});
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(ModelBackedJvmJarBinary.class, binary -> {
				binary.getJarTask().configure(configureDependsOn((Callable<Object>) component.getSources().withType(KotlinSourceSetSpec.class).map(it -> it.getCompileTask().get())::get));
			});
		});

		artifacts(project).withType(ModelBackedJniJarBinary.class).configureEach(binary -> {
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

		artifacts(project).withType(ModelBackedJvmJarBinary.class).configureEach(binary -> {
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
			component.getBinaries().configureEach(ModelBackedJvmJarBinary.class, binary -> {
				binary.getJarTask().configure(task -> {
					if (component.getBuildVariants().get().size() == 1) {
						task.setDescription(String.format("Assembles a JAR archive containing the classes and shared library for %s.", binary));
					}
				});
			});
		});

		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getBinaries().configureEach(ModelBackedJvmJarBinary.class, binary -> {
				binary.getJarTask().configure(task -> task.getArchiveBaseName().set(component.getBaseName()));
			});
		});
		val registerJvmJarBinaryAction = new Action<AppliedPlugin>() {
			@Override
			public void execute(AppliedPlugin ignored) {
				project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(JniLibraryComponentInternal.class), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					val binaryIdentifier = identifier.get().child(ElementName.ofMain("jvmJar"));
					val jvmJar = registry.instantiate(project.getExtensions().getByType(JvmJarBinaryRegistrationFactory.class).create(binaryIdentifier)
						.withComponent(new ParentComponent(entity))
						.withComponentTag(ExcludeFromQualifyingNameTag.class)
						.build());
					ModelStates.register(jvmJar);
					entity.addComponent(new JvmJarArtifactComponent(jvmJar));
				})));
			}
		};
		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), registerJvmJarBinaryAction).execute(project);

		// Assemble task configuration
		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getAssembleTask().configure(configureDependsOn((Callable<Object>) component.getBinaries().filter(it -> it instanceof ModelBackedJvmJarBinary)::get));
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getAssembleTask().configure(configureDependsOn(variant.getJavaNativeInterfaceJar()));
		});

		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), ignored -> {
			components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
				component.getVariants().configureEach(JniLibraryInternal.class, variant -> {
					variant.getAssembleTask().configure(configureDependsOn((Callable<Object>) component.getBinaries().filter(it -> it instanceof ModelBackedJvmJarBinary)::get));
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
		// TODO: We should limit to JNILibrary variant
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(IsVariant.class), (entity, id, tag) -> {
			val identifier = (VariantIdentifier) id.get();
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			val sharedLibrary = registry.register(newEntity(identifier.child(ElementName.ofMain("sharedLibrary")), SharedLibraryBinaryInternal.class, it -> it.ownedBy(entity)
				.displayName("shared library binary")
				.withComponent(new BuildVariantComponent(identifier.getBuildVariant()))
				.withTag(ExcludeFromQualifyingNameTag.class)
			));
		})));
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getNativeRuntimeFiles().from(variant.getSharedLibrary().getLinkTask().flatMap(LinkSharedLibrary::getLinkedFile));
			variant.getNativeRuntimeFiles().from((Callable<Object>) () -> variant.getSharedLibrary().getRuntimeLibraries().getAsConfiguration().getIncoming().getFiles());
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getDevelopmentBinary().convention(variant.getJavaNativeInterfaceJar());
		});
		variants(project).withType(JniLibraryInternal.class).configureEach(variant -> {
			variant.getBinaries().configureEach(ModelBackedJniJarBinary.class, binary -> {
				binary.getJarTask().configure(task -> task.getArchiveBaseName()
					.set(variant.getBaseName().map(baseName -> baseName + variant.getIdentifier().getAmbiguousDimensions().getAsKebabCase().map(it -> "-" + it).orElse(""))));
			});
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(JniLibraryInternal.class), ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(IsVariant.class), (entity, projection, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val binaryIdentifier = identifier.get().child(ElementName.ofMain("jniJar"));
			val jniJar = registry.instantiate(project.getExtensions().getByType(JniJarBinaryRegistrationFactory.class).create(binaryIdentifier)
				.withComponent(new ParentComponent(entity))
				.withComponentTag(JniJarArtifactTag.class)
				.withComponentTag(ExcludeFromQualifyingNameTag.class)
				.build());
			entity.addComponent(new JniJarArtifactComponent(jniJar));
			ModelStates.register(jniJar);
		})));

		val unbuildableWarningService = (Provider<UnbuildableWarningService>) project.getGradle().getSharedServices().getRegistrations().getByName(UnbuildableWarningService.class.getSimpleName()).getService();

		components(project).withType(JniLibraryComponentInternal.class).configureEach(component -> {
			component.getVariants().configureEach(JniLibraryInternal.class, variant -> {
				variant.getJar().getJarTask().configure(configureJarTaskUsing(project.provider(() -> variant), unbuildableWarningService.map(it -> {
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
