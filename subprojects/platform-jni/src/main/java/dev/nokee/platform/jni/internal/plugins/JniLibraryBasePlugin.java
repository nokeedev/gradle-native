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
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.language.nativebase.internal.HasConfigurableHeadersPropertyComponent;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.actions.ModelActionSystem;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelProperty;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.BinaryIdentity;
import dev.nokee.platform.base.internal.BuildVariantComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.CompileTaskTag;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.IsTask;
import dev.nokee.platform.base.internal.IsVariant;
import dev.nokee.platform.base.internal.TaskRegistrationFactory;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.Variants;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ExtendsFromParentConfigurationAction;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.platform.jni.internal.AssembleTask;
import dev.nokee.platform.jni.internal.GeneratedJniHeadersComponent;
import dev.nokee.platform.jni.internal.JarTaskComponent;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryVariantRegistrationFactory;
import dev.nokee.platform.jni.internal.JniJarArtifactComponent;
import dev.nokee.platform.jni.internal.JniJarBinaryRegistrationFactory;
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
import dev.nokee.platform.jni.internal.JniLibraryInternal;
import dev.nokee.platform.jni.internal.JvmJarArtifactComponent;
import dev.nokee.platform.jni.internal.JvmJarBinaryRegistrationFactory;
import dev.nokee.platform.jni.internal.ModelBackedJniJarBinary;
import dev.nokee.platform.jni.internal.ModelBackedJvmJarBinary;
import dev.nokee.platform.jni.internal.MultiVariantTag;
import dev.nokee.platform.jni.internal.actions.WhenPlugin;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.DependentRuntimeLibraries;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.linking.LinkLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import dev.nokee.platform.nativebase.internal.rules.WarnUnbuildableLogger;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.platform.NativePlatform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.actions.ModelSpec.ownedBy;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.GradlePropertyTypes.property;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.platform.jni.internal.actions.WhenPlugin.any;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.RunnableUtils.onlyOnce;
import static dev.nokee.utils.TaskUtils.configureBuildGroup;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureDescription;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class JniLibraryBasePlugin implements Plugin<Project> {
	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(LanguageBasePlugin.class);
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		project.getPluginManager().apply(JvmLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		project.getExtensions().add("__nokee_jniJarBinaryFactory", new JniJarBinaryRegistrationFactory());
		project.getExtensions().add("__nokee_jvmJarBinaryFactory", new JvmJarBinaryRegistrationFactory());
		project.getExtensions().add("__nokee_jniLibraryComponentFactory", new JavaNativeInterfaceLibraryComponentRegistrationFactory(project));
		project.getExtensions().add("__nokee_jniLibraryVariantFactory", new JavaNativeInterfaceLibraryVariantRegistrationFactory(project));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionSystem.updateSelectorForTag(CompileTaskTag.class));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(JniLibraryInternal.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsVariant.class), (entity, projection, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val binaryIdentifier = BinaryIdentifier.of(identifier.get(), BinaryIdentity.ofMain("jniJar", "JNI JAR binary"));
			val jniJar = registry.instantiate(project.getExtensions().getByType(JniJarBinaryRegistrationFactory.class).create(binaryIdentifier)
				.withComponent(new ParentComponent(entity))
				.build());
			registry.instantiate(configure(jniJar.getId(), JniJarBinary.class, binary -> {
				binary.getJarTask().configure(task -> {
					task.getArchiveBaseName().set(project.provider(() -> {
						val baseName = ModelProperties.getProperty(entity, "baseName").as(String.class).get();
						return baseName + ((VariantIdentifier<?>) identifier.get()).getAmbiguousDimensions().getAsKebabCase().map(it -> "-" + it).orElse("");
					}));
				});
			}));
			ModelStates.register(jniJar);
			entity.addComponent(new JniJarArtifactComponent(jniJar));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelBackedJniJarBinary.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsBinary.class), (entity, projection, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val taskRegistrationFactory = project.getExtensions().getByType(TaskRegistrationFactory.class);
			val jarTask = registry.instantiate(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("jar"), identifier.get()), Jar.class).withComponent(new ElementNameComponent("jar")).build());
			registry.instantiate(configure(jarTask.getId(), Jar.class, configureBuildGroup()));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				task.getDestinationDirectory().convention(task.getProject().getLayout().getBuildDirectory().dir("libs"));
			}));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				task.getArchiveBaseName().convention(((BinaryIdentifier<?>) identifier.get()).getName().get());
			}));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				if (task.getDescription() == null) {
					task.setDescription(String.format("Assembles a JAR archive containing the shared library for %s.", identifier.get()));
				}
			}));
			ModelStates.register(jarTask);
			entity.addComponent(new JarTaskComponent(jarTask));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(ModelBackedJvmJarBinary.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsBinary.class), (entity, projection, identifier, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val taskRegistrationFactory = project.getExtensions().getByType(TaskRegistrationFactory.class);
			val jarTask = registry.instantiate(taskRegistrationFactory.create(TaskIdentifier.of(TaskName.of("jar"), identifier.get()), Jar.class).withComponent(new ElementNameComponent("jar")).build());
			registry.instantiate(configure(jarTask.getId(), Jar.class, configureBuildGroup()));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				task.getDestinationDirectory().convention(task.getProject().getLayout().getBuildDirectory().dir("libs"));
			}));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> task.getArchiveBaseName().convention(((BinaryIdentifier<?>) identifier.get()).getName().get())));
			registry.instantiate(configure(jarTask.getId(), Jar.class, task -> {
				if (task.getDescription() == null) {
					task.setDescription(String.format("Assembles a JAR archive containing the classes for %s.", identifier.get()));
				}
			}));
			ModelStates.register(jarTask);
			entity.addComponent(new JarTaskComponent(jarTask));
		})));


		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(Variants.class), ModelComponentReference.of(JvmJarArtifactComponent.class), (entity, variants, jvmJar) -> {
			if (Iterables.size(variants) == 1) {
				project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelAction.configure(jvmJar.get().getId(), JvmJarBinary.class, binary -> {
					binary.getJarTask().configure(configureDescription("Assembles a JAR archive containing the classes and shared library for %s.", ModelNodes.of(binary).get(IdentifierComponent.class).get()));
				}));
			}
		}));

		val registerJvmJarBinaryAction = new Action<AppliedPlugin>() {
			@Override
			public void execute(AppliedPlugin ignored) {
				project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.ofProjection(JniLibraryComponentInternal.class), ModelComponentReference.of(IdentifierComponent.class), (entity, projection, identifier) -> {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					val binaryIdentifier = BinaryIdentifier.of(identifier.get(), BinaryIdentity.ofMain("jvmJar", "JVM JAR binary"));
					val jvmJar = registry.instantiate(project.getExtensions().getByType(JvmJarBinaryRegistrationFactory.class).create(binaryIdentifier)
						.withComponent(new ParentComponent(entity))
						.build());
					registry.instantiate(configure(jvmJar.getId(), JvmJarBinary.class, binary -> {
						binary.getJarTask().configure(task -> task.getArchiveBaseName().set(project.provider(() -> {
							return ModelProperties.getProperty(entity, "baseName").as(String.class).get();
						})));
					}));
					ModelStates.register(jvmJar);
					entity.addComponent(new JvmJarArtifactComponent(jvmJar));
				})));
			}
		};
		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), registerJvmJarBinaryAction).execute(project);

		// Assemble task configuration
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(JvmJarArtifactComponent.class), ModelComponentReference.of(AssembleTask.class), (entity, jvmJar, assembleTask) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.instantiate(configure(assembleTask.get().getId(), Task.class, configureDependsOn((Callable<Object>) () -> ModelNodeUtils.get(jvmJar.get(), JvmJarBinary.class))));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of( ModelComponentReference.of(JniJarArtifactComponent.class), ModelComponentReference.of(AssembleTask.class), ModelComponentReference.of(MultiVariantTag.class), (entity, jniJar, assembleTask, tag) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.instantiate(configure(assembleTask.get().getId(), Task.class, configureDependsOn((Callable<Object>) () -> ModelNodeUtils.get(jniJar.get(), JniJarBinary.class))));
		}));

		new WhenPlugin(any("java", "groovy", "org.jetbrains.kotlin.jvm"), ignored -> {
			// ComponentFromEntity<JvmJarArtifactComponent.class> read-only from ParentComponent
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(JniJarArtifactComponent.class), ModelComponentReference.of(AssembleTask.class), (entity, parent, jniJar, assembleTask) -> {
				project.getExtensions().getByType(ModelRegistry.class).instantiate(configure(assembleTask.get().getId(), Task.class, configureDependsOn((Callable<Object>) () -> ModelNodeUtils.get(parent.get().get(JvmJarArtifactComponent.class).get(), JvmJarBinary.class))));
			}));
		}).execute(project);

		// Variant rules
		// TODO: We should limit to JNILibrary variant
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsVariant.class), ModelComponentReference.of(ModelPathComponent.class), (entity, id, tag, path) -> {
			val identifier = (VariantIdentifier<?>) id.get();
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			if (!((VariantIdentifier<?>) id.get()).getUnambiguousName().isEmpty()) {
				entity.addComponent(MultiVariantTag.tag());
			}

			val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
			val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeImplementation"), identifier)));
			val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeLinkOnly"), identifier)));
			val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeRuntimeOnly"), identifier)));

			val sharedLibrary = registry.register(ModelRegistration.builder().mergeFrom(project.getExtensions().getByType(SharedLibraryBinaryRegistrationFactory.class).create(BinaryIdentifier.of(identifier, BinaryIdentity.ofMain("sharedLibrary", "shared library binary")))).withComponent(ExcludeFromQualifyingNameTag.tag()).withComponent(new BuildVariantComponent(identifier.getBuildVariant())).build());
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsBinary.class), ModelComponentReference.of(LinkLibrariesConfiguration.class), (e, idd, t, linkLibraries) -> {
				if (idd.get().equals(ModelNodes.of(sharedLibrary).get(IdentifierComponent.class).get())) {
					linkLibraries.configure(configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class)));
				}
			}));
			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsBinary.class), ModelComponentReference.of(RuntimeLibrariesConfiguration.class), (e, idd, t, runtimeLibraries) -> {
				if (idd.get().equals(ModelNodes.of(sharedLibrary).get(IdentifierComponent.class).get())) {
					project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelAction.configure(runtimeLibraries.get().getId(), Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))));
				}
			}));
			val sharedLibraryTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(identifier, "sharedLibrary"), Task.class).build());
			sharedLibraryTask.configure(Task.class, configureBuildGroup());
			sharedLibraryTask.configure(Task.class, configureDescription("Assembles the shared library binary of %s.", identifier.getDisplayName()));
			sharedLibraryTask.configure(Task.class, configureDependsOn(sharedLibrary.as(SharedLibraryBinary.class)));

			val developmentBinaryProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "developmentBinary"), Binary.class));
			((ModelProperty<Binary>) developmentBinaryProperty).asProperty(property(of(Binary.class))).convention(project.provider(() -> new Object()).flatMap(it -> ModelElements.of(entity).element("jniJar", JniJarBinary.class).asProvider()));

			project.getPlugins().withType(NativeLanguagePlugin.class, new Action<NativeLanguagePlugin>() {
				private ModelElement compileOnly = null;

				@Override
				public void execute(NativeLanguagePlugin appliedPlugin) {
					if (compileOnly == null) {
						compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeCompileOnly"), identifier)));
					}
					val sourceSet = registry.register(project.getExtensions().getByType(appliedPlugin.getRegistrationFactoryType()).create(identifier));
					val sourceSetIdentifier = (LanguageSourceSetIdentifier) ModelNodes.of(sourceSet).get(IdentifierComponent.class).get();
					val configurer = project.getExtensions().getByType(ModelConfigurer.class);
					configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsDependencyBucket.class), ModelComponentReference.ofProjection(Configuration.class), (e, i, tag, configuration) -> {
						if (((DependencyBucketIdentifier) i.get()).getOwnerIdentifier().equals(sourceSetIdentifier)) {
							registry.instantiate(ModelAction.configure(e.getId(), Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))));
							registry.instantiate(ModelAction.configure(e.getId(), Configuration.class, ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects())));
							registry.instantiate(ModelAction.configure(e.getId(), Configuration.class, ConfigurationUtilsEx::configureAsGradleDebugCompatible));
						}
					}));
					configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsTask.class), ModelComponentReference.ofProjection(TaskProvider.class), (e, i, t, p) -> {
						if (((TaskIdentifier<?>) i.get()).getOwnerIdentifier().equals(sourceSetIdentifier)) {
							e.addComponent(CompileTaskTag.tag());
							NamedDomainObjectProvider<Task> compileTask = p.get(of(NamedDomainObjectProvider.class));
							compileTask.configure(configureTargetPlatform(set(fromBuildVariant(identifier.getBuildVariant()))));
						}
					}));
				}
			});

			val objectsTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(identifier, "objects"), Task.class).build());
			objectsTask.configure(Task.class, configureDependsOn(sharedLibrary.as(SharedLibraryBinary.class).flatMap(binary -> binary.getCompileTasks().filter(it -> it instanceof HasObjectFiles))));
			objectsTask.configure(Task.class, configureBuildGroup());
			objectsTask.configure(Task.class, configureDescription("Assembles the object files of %s.", identifier.getDisplayName()));

			val assembleTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier), Task.class).build());
			assembleTask.configure(Task.class, task -> {
				task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
				if (task.getDescription() == null) {
					task.setDescription(String.format("Assembles the outputs of %s.", identifier.getDisplayName()));
				}
			});
			entity.addComponent(new AssembleTask(ModelNodes.of(assembleTask)));

			val nativeRuntimeFiles = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createFileCollectionProperty(ModelPropertyIdentifier.of(identifier, "nativeRuntimeFiles")));
			((ModelProperty<Set<File>>) nativeRuntimeFiles).asProperty(of(ConfigurableFileCollection.class)).from(sharedLibrary.as(SharedLibraryBinary.class).flatMap(SharedLibraryBinary::getLinkTask).flatMap(LinkSharedLibrary::getLinkedFile));
			((ModelProperty<Set<File>>) nativeRuntimeFiles).asProperty(of(ConfigurableFileCollection.class)).from((Callable<Object>) () -> ModelNodes.of(sharedLibrary).get(DependentRuntimeLibraries.class));

			ModelProperties.getProperty(sharedLibrary, "baseName").asProperty(property(of(String.class))).convention(project.provider(() -> new Object()).flatMap(it -> ModelProperties.getProperty(entity, "baseName").as(String.class).asProvider()));

			val resourcePathProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "resourcePath"), String.class));
			((ModelProperty<String>) resourcePathProperty).asProperty(property(of(String.class))).convention(identifier.getAmbiguousDimensions().getAsKebabCase().orElse(""));

			sharedLibrary.configure(SharedLibraryBinary.class, binary -> binary.getBaseName().convention(project.provider(() -> new Object()).flatMap(it -> ModelProperties.getProperty(entity, "baseName").as(String.class).asProvider())));

			entity.addComponent(createdUsing(of(JniLibraryInternal.class), () -> project.getObjects().newInstance(JniLibraryInternal.class, identifier, project.getObjects())));


			project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsVariant.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.ofProjection(JniLibrary.class), (e, i, t, s, p, library) -> {
				if (i.get().equals(identifier)) {
					val unbuildableMainComponentLogger = new WarnUnbuildableLogger(identifier.getComponentIdentifier());

					ModelNodeUtils.get(e, JniLibrary.class).getJavaNativeInterfaceJar().getJarTask().configure(configureJarTaskUsing(project.provider(() -> ModelNodeUtils.get(e, JniLibrary.class)), unbuildableMainComponentLogger));
				}
			}));

			registry.instantiate(configureMatching(ownedBy(entity.getId()).and(subtypeOf(of(Configuration.class))), new ExtendsFromParentConfigurationAction(project, path.get())));
		})));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(IsVariant.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, path, tag, ignored) -> {
			entity.addComponent(new ModelBackedNativeIncomingDependencies(path.get(), project.getObjects(), project.getProviders(), project.getExtensions().getByType(ModelLookup.class), s -> "native" + capitalize(s)));
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(HasConfigurableHeadersPropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, headers, parent) -> {
			// Attach generated JNI headers
			((ConfigurableFileCollection) headers.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				return ParentUtils.stream(parent).filter(it -> it.has(GeneratedJniHeadersComponent.class)).map(it -> it.get(GeneratedJniHeadersComponent.class).get()).collect(Collectors.toList());
			});
		}));
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

	private static Action<Jar> configureJarTaskUsing(Provider<JniLibrary> library, WarnUnbuildableLogger logger) {
		val runnableLogger = onlyOnce(logger::warn);
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
						runnableLogger.run();
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
