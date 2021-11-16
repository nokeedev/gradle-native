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
package dev.nokee.platform.jni.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.NativePlatformFactory;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.jni.JniJarBinary;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.DependentRuntimeLibraries;
import dev.nokee.platform.nativebase.internal.LinkLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.RuntimeLibrariesConfiguration;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryRegistrationFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.rules.WarnUnbuildableLogger;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.logging.Logger;
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
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.RunnableUtils.onlyOnce;
import static dev.nokee.utils.TaskUtils.*;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public final class JavaNativeInterfaceLibraryVariantRegistrationFactory {
	private final Project project;

	public JavaNativeInterfaceLibraryVariantRegistrationFactory(Project project) {
		this.project = project;
	}

	public ModelRegistration create(VariantIdentifier<?> identifier) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.hasAxisValue(TARGET_MACHINE_COORDINATE_AXIS));

		return ModelRegistration.builder()
			.withComponent(DomainObjectIdentifierUtils.toPath(identifier))
			.withComponent(IsVariant.tag())
			.withComponent(identifier)
			.withComponent(new FullyQualifiedName(VariantNamer.INSTANCE.determineName(identifier)))
			.withComponent(createdUsing(of(JniLibraryInternal.class), () -> project.getObjects().newInstance(JniLibraryInternal.class, identifier, project.getObjects(), project.getConfigurations(), project.getProviders(), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskViewFactory.class))))
			.withComponent(createdUsing(of(new TypeOf<Provider<JniLibrary>>() {}), () -> {
				val entity = ModelNodeContext.getCurrentModelNode();
				return project.getProviders().provider(() -> ModelNodeUtils.get(entity, JniLibraryInternal.class));
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(LanguageSourceSetIdentifier.class), ModelComponentReference.ofProjection(LanguageSourceSet.class).asDomainObject(), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, id, sourceSet, ignored) -> {
				if (DomainObjectIdentifierUtils.isDescendent(id, identifier) && sourceSet instanceof HasHeaders) {
					((HasHeaders) sourceSet).getHeaders().from("src/" + identifier.getComponentIdentifier().getName() + "/headers");
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, path, id, ignored) -> {
				if (id.equals(identifier)) {
					entity.addComponent(new ModelBackedNativeIncomingDependencies(path, project.getObjects(), project.getProviders(), project.getExtensions().getByType(ModelLookup.class), s -> "native" + capitalize(s)));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofProjection(JniLibrary.class).asProvider(), (entity, id, state, path, library) -> {
				if (id.equals(identifier)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);

					registry.register(project.getExtensions().getByType(ComponentBinariesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "binaries")));
					val jniJar = registry.register(project.getExtensions().getByType(JniJarBinaryRegistrationFactory.class).create(BinaryIdentifier.of(identifier, BinaryIdentity.ofMain("jniJar", "JNI JAR binary"))));
					val developmentBinaryProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "developmentBinary"), Binary.class));
					developmentBinaryProperty.configure(Property.class, prop -> prop.convention(jniJar.as(JniJarBinary.class).map(noOpTransformer())));

					registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "sources")));

					val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
					registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "dependencies"), JavaNativeInterfaceNativeComponentDependencies.class, ModelBackedJavaNativeInterfaceNativeComponentDependencies::new));
					val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeImplementation"), identifier)));
					val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeLinkOnly"), identifier)));
					val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeRuntimeOnly"), identifier)));

					val sharedLibrary = registry.register(project.getExtensions().getByType(SharedLibraryBinaryRegistrationFactory.class).create(BinaryIdentifier.of(identifier, BinaryIdentity.ofMain("sharedLibrary", "shared library binary"))));
					ModelNodes.of(sharedLibrary).addComponent(identifier.getBuildVariant());
					project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(BinaryIdentifier.class), ModelComponentReference.of(LinkLibrariesConfiguration.class), (e, idd, linkLibraries) -> {
						if (idd.equals(ModelNodes.of(sharedLibrary).getComponent(DomainObjectIdentifier.class))) {
							linkLibraries.configure(configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class)));
						}
					}));
					project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(BinaryIdentifier.class), ModelComponentReference.of(RuntimeLibrariesConfiguration.class), (e, idd, runtimeLibraries) -> {
						if (idd.equals(ModelNodes.of(sharedLibrary).getComponent(DomainObjectIdentifier.class))) {
							runtimeLibraries.configure(configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class)));
						}
					}));
					registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "sharedLibrary"), ModelNodes.of(sharedLibrary)));
					val sharedLibraryTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(identifier, "sharedLibrary"), Task.class).build());
					sharedLibraryTask.configure(Task.class, configureBuildGroup());
					sharedLibraryTask.configure(Task.class, configureDescription("Assembles the shared library binary of %s.", identifier.getDisplayName()));
					sharedLibraryTask.configure(Task.class, configureDependsOn(sharedLibrary.as(SharedLibraryBinary.class)));

					project.getPlugins().withType(NativeLanguagePlugin.class, new Action<NativeLanguagePlugin>() {
						private ModelElement compileOnly = null;

						@Override
						public void execute(NativeLanguagePlugin appliedPlugin) {
							if (compileOnly == null) {
								compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeCompileOnly"), identifier)));
							}
							val sourceSet = registry.register(project.getExtensions().getByType(appliedPlugin.getRegistrationFactoryType()).create(identifier));
							val sourceSetIdentifier = ModelNodes.of(sourceSet).getComponent(LanguageSourceSetIdentifier.class);
							val configurer = project.getExtensions().getByType(ModelConfigurer.class);
							configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(DependencyBucketIdentifier.class), ModelComponentReference.ofProjection(Configuration.class).asConfigurableProvider(), (e, i, configuration) -> {
								if (i.getOwnerIdentifier().equals(sourceSetIdentifier)) {
									configuration.configure(configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class)));
									configuration.configure(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()));
									configuration.configure(ConfigurationUtilsEx::configureAsGradleDebugCompatible);
								}
							}));
							configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(TaskIdentifier.class), ModelComponentReference.ofInstance(projectionOf(TaskProvider.class)), (e, i, p) -> {
								if (i.getOwnerIdentifier().equals(sourceSetIdentifier)) {
									NamedDomainObjectProvider<Task> compileTask = p.get(of(NamedDomainObjectProvider.class));
									compileTask.configure(configureTargetPlatform(set(fromBuildVariant(identifier.getBuildVariant()))));
									registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(ModelPropertyIdentifier.of(ModelNodes.of(sharedLibrary).getComponent(BinaryIdentifier.class), "compileTasks"), "compile" + capitalize(sourceSetIdentifier.getName().get())), e));
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
					registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "assembleTask"), ModelNodes.of(assembleTask)));

					val nativeRuntimeFiles = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createFileCollectionProperty(ModelPropertyIdentifier.of(identifier, "nativeRuntimeFiles")));
					nativeRuntimeFiles.configure(ConfigurableFileCollection.class, files -> files.from(sharedLibrary.as(SharedLibraryBinary.class).flatMap(SharedLibraryBinary::getLinkTask).flatMap(LinkSharedLibrary::getLinkedFile)));
					nativeRuntimeFiles.configure(ConfigurableFileCollection.class, files -> files.from((Callable<Object>) () -> ModelNodes.of(sharedLibrary).getComponent(DependentRuntimeLibraries.class)));

					val baseNameProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "baseName"), String.class));
					baseNameProperty.configure(Property.class, prop -> prop.convention(identifier.getUnambiguousName()));
					ModelProperties.getProperty(sharedLibrary, "baseName").configure(Property.class, prop -> prop.convention(baseNameProperty.as(String.class).map(noOpTransformer())));

					val resourcePathProperty = registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "resourcePath"), String.class));
					resourcePathProperty.configure(Property.class, prop -> prop.convention(""));

					jniJar.configure(JniJarBinary.class, binary -> {
						binary.getJarTask().configure(task -> {
							task.getArchiveBaseName().set(baseNameProperty.as(String.class).map(baseName -> baseName + identifier.getAmbiguousDimensions().getAsKebabCase().map(it -> "-" + it).orElse("")));
						});
						val unbuildableMainComponentLogger = new WarnUnbuildableLogger(identifier.getComponentIdentifier());
						binary.getJarTask().configure(configureJarTaskUsing(library, unbuildableMainComponentLogger));
					});

					registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "javaNativeInterfaceJar"), ModelNodes.of(jniJar)));

					sharedLibrary.configure(SharedLibraryBinary.class, binary -> binary.getBaseName().convention(baseNameProperty.as(String.class).map(noOpTransformer())));

					registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(id, "tasks")));

					whenElementKnown(entity, ModelActionWithInputs.of(ModelComponentReference.ofAny(projectionOf(Configuration.class)), ModelComponentReference.of(ModelPath.class), (e, ig, p) -> {
						((NamedDomainObjectProvider<Configuration>) ModelNodeUtils.get(e, NamedDomainObjectProvider.class)).configure(configuration -> {
							val parentConfigurationResult = project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withPath(path.getParent().get().child(p.getName()))));
							Optional.ofNullable(Iterables.getOnlyElement(parentConfigurationResult.get(), null)).ifPresent(parentConfigurationEntity -> {
								val parentConfiguration = ModelNodeUtils.get(parentConfigurationEntity, Configuration.class);
								if (!parentConfiguration.getName().equals(configuration.getName())) {
									configuration.extendsFrom(parentConfiguration);
								}
							});
						});
					}));
				}
			}))
			.build();
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
					if (it.getTargetMachine().getOperatingSystemFamily().equals(OperatingSystemFamily.forName(System.getProperty("os.name")))) {
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

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}
}
