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

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.c.internal.plugins.CHeaderSetRegistrationFactory;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.c.internal.plugins.CLanguagePlugin;
import dev.nokee.language.cpp.internal.plugins.CppLanguagePlugin;
import dev.nokee.language.jvm.internal.GroovySourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.JavaSourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.KotlinSourceSetRegistrationFactory;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.ObjectSourceSet;
import dev.nokee.language.nativebase.internal.ToolChainSelectorInternal;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguagePlugin;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguagePlugin;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.*;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelNodeBackedKnownDomainObject;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskViewFactory;
import dev.nokee.platform.jni.*;
import dev.nokee.platform.jni.internal.*;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.NativeLanguageRules;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.WarnUnbuildableLogger;
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask;
import dev.nokee.platform.nativebase.internal.tasks.SharedLibraryLifecycleTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinFrameworkResolutionSupportPlugin;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.MutationGuards;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.plugins.NativeBasePlugin;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static dev.nokee.model.internal.core.ModelNodeUtils.applyTo;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.*;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.*;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.jni.internal.plugins.JniLibraryPlugin.IncompatiblePluginsAdvice.*;
import static dev.nokee.platform.jni.internal.plugins.JvmIncludeRoots.jvmIncludes;
import static dev.nokee.platform.jni.internal.plugins.NativeCompileTaskProperties.includeRoots;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.*;
import static dev.nokee.runtime.nativebase.TargetMachine.TARGET_MACHINE_COORDINATE_AXIS;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static dev.nokee.utils.RunnableUtils.onlyOnce;
import static dev.nokee.utils.TaskUtils.*;
import static dev.nokee.utils.TransformerUtils.transformEach;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;

public class JniLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";
	private static final Set<Class<? extends Plugin<Project>>> NATIVE_LANGUAGE_PLUGINS = ImmutableSet.of(CLanguagePlugin.class, CppLanguagePlugin.class, ObjectiveCLanguagePlugin.class, ObjectiveCppLanguagePlugin.class);

	private final ToolChainSelectorInternal toolChainSelectorInternal;
	@Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencyHandler;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;

	@Inject
	public JniLibraryPlugin(ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ConfigurationContainer configurations, ProjectLayout layout, DependencyHandler dependencyHandler) {
		this.objects = objects;
		this.providers = providers;
		this.tasks = tasks;
		this.configurations = configurations;
		this.layout = layout;
		this.toolChainSelectorInternal = objects.newInstance(ToolChainSelectorInternal.class);
		this.dependencyHandler = dependencyHandler;
	}

	@Override
	public void apply(Project project) {
		IncompatiblePluginUsage.forProject(project)
			.assertPluginIds(SOFTWARE_MODEL_PLUGIN_IDS, IncompatiblePluginsAdvice::forSoftwareModelNativePlugins)
			.assertPluginId(JAVA_APPLICATION_PLUGIN_ID, IncompatiblePluginsAdvice::forJavaApplicationEntryPointPlugin)
			.assertPluginId(JAVA_LIBRARY_PLUGIN_ID, IncompatiblePluginsAdvice::forJavaLibraryEntryPointPlugin)
			.assertPluginIds(CURRENT_MODEL_PLUGIN_IDS, IncompatiblePluginsAdvice::forCurrentModelNativePlugins)
			.assertPluginClass(NativeBasePlugin.class, IncompatiblePluginsAdvice::forNativeBasePlugin);

		project.getPluginManager().apply("base");
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);
		project.getPluginManager().apply(NativeRuntimePlugin.class);

		val extension = registerExtension(project);
		TaskRegistry taskRegistry = project.getExtensions().getByType(TaskRegistry.class);

		// TODO: On `java` apply, just apply the `java-library` (but don't allow other users to apply it
		project.getPluginManager().withPlugin("java", appliedPlugin -> configureJavaJniRuntime(project, extension));
		project.getPluginManager().withPlugin("java", appliedPlugin -> registerJniHeaderSourceSet(project, extension));
		project.getPluginManager().withPlugin("java", appliedPlugin -> registerJvmHeaderSourceSet(extension));
		project.getPlugins().whenPluginAdded(appliedPlugin -> {
			if (isNativeLanguagePlugin(appliedPlugin)) {
				project.getPluginManager().apply(DarwinFrameworkResolutionSupportPlugin.class);
			}
		});

		whenElementKnown(extension, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(JniLibrary.class)), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (entity, variantIdentifier, variantProjection, ignored) -> {
			val knownVariant = new ModelNodeBackedKnownDomainObject<>(ModelType.of(JniLibraryInternal.class), entity);
			val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val sharedLibraryBinaryIdentifier = BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, variantIdentifier);
			eventPublisher.publish(new DomainObjectDiscovered<>(sharedLibraryBinaryIdentifier));

			if (project.getPluginManager().hasPlugin("java") && extension.getTargetMachines().get().size() == 1) {
				val jniJarIdentifier = BinaryIdentifier.of(BinaryName.of("jniJar"), DefaultJvmJarBinary.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(jniJarIdentifier));
				val binary = createJvmBinary(project);
				registry.register(ModelRegistration.builder()
					.withComponent(jniJarIdentifier)
					.withComponent(DomainObjectIdentifierUtils.toPath(jniJarIdentifier))
					.withComponent(IsBinary.tag())
					.withComponent(createdUsing(ModelType.of(DefaultJvmJarBinary.class), () -> binary))
					.build());
				knownVariant.configure(variant -> {
					variant.addJniJarBinary(binary);
				});
			} else {
				val jniJarIdentifier = BinaryIdentifier.of(BinaryName.of("jniJar"), DefaultJniJarBinary.class, variantIdentifier);
				eventPublisher.publish(new DomainObjectDiscovered<>(jniJarIdentifier));
				TaskProvider<Jar> jarTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("jar"), Jar.class, variantIdentifier));
				val jniBinary = getObjects().newInstance(DefaultJniJarBinary.class, jarTask);
				registry.register(ModelRegistration.builder()
					.withComponent(jniJarIdentifier)
					.withComponent(DomainObjectIdentifierUtils.toPath(jniJarIdentifier))
					.withComponent(IsBinary.tag())
					.withComponent(createdUsing(ModelType.of(DefaultJniJarBinary.class), () -> jniBinary))
					.build());
				knownVariant.configure(it -> it.addJniJarBinary(jniBinary));

				if (project.getPluginManager().hasPlugin("java")) {
					val jvmJarIdentifier = BinaryIdentifier.of(BinaryName.of("jvmJar"), DefaultJvmJarBinary.class, variantIdentifier);
					eventPublisher.publish(new DomainObjectDiscovered<>(jvmJarIdentifier));
					val jvmBinary = createJvmBinary(project);
					registry.register(ModelRegistration.builder()
						.withComponent(jvmJarIdentifier)
						.withComponent(DomainObjectIdentifierUtils.toPath(jvmJarIdentifier))
						.withComponent(IsBinary.tag())
						.withComponent(createdUsing(ModelType.of(DefaultJvmJarBinary.class), () -> jvmBinary))
						.build());
					knownVariant.configure(variant -> {
						variant.addJvmJarBinary(jvmBinary);
					});
				}
//					if (proj.getPluginManager().hasPlugin("java")) {
//						library.getAssembleTask().configure(task -> task.dependsOn(project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class)));
//					} else {
//						// FIXME: There is a gap here, if the project doesn't have any JVM plugin applied but specify multiple target machine what is expected?
//						//   Only JNI Jar? or an empty JVM Jar and JNI Jar?... Hmmm....
//					}
			}

			TaskProvider<LinkSharedLibraryTask> linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, variantIdentifier));
		}));

		extension.getVariants().configureEach(JniLibraryInternal.class, variant -> {
			// Build all language source set
			val objectSourceSets = getObjects().domainObjectSet(ObjectSourceSet.class);
			if (project.getPlugins().stream().anyMatch(appliedPlugin -> isNativeLanguagePlugin(appliedPlugin))) {
				objectSourceSets.addAll(new NativeLanguageRules(taskRegistry, objects, variant.getIdentifier()).apply(extension.getSources()));
			}

			TaskProvider<LinkSharedLibraryTask> linkTask = tasks.named(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, variant.getIdentifier()).getTaskName(), LinkSharedLibraryTask.class);
			MutationGuards.of(tasks).withMutationEnabled(ignored -> {
				variant.registerSharedLibraryBinary(objectSourceSets, linkTask, (NativeIncomingDependencies)variant.getResolvableDependencies());

				variant.getSharedLibrary().getCompileTasks().configureEach(NativeSourceCompileTask.class, task -> {
					val taskInternal = (AbstractNativeCompileTask) task;
					taskInternal.getIncludes().from(extension.getSources().filter(it -> it instanceof NativeHeaderSet).map(transformEach(LanguageSourceSet::getSourceDirectories)));
				});
			}).execute(null);
		});

		val unbuildableMainComponentLogger = new WarnUnbuildableLogger(ModelNodeUtils.get(ModelNodes.of(extension), JniLibraryComponentInternal.class).getIdentifier());
		project.afterEvaluate(proj -> {
			Set<TargetMachine> targetMachines = extension.getTargetMachines().get();
			val projection = ModelNodeUtils.get(ModelNodes.of(extension), JniLibraryComponentInternal.class);

			ModelNodeUtils.finalizeProjections(ModelNodes.of(extension));
			whenElementKnown(extension, ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.ofAny(projectionOf(JniLibrary.class)), (entity, variantIdentifier, variantProjection) -> {
				val knownVariant = new ModelNodeBackedKnownDomainObject<>(of(JniLibraryInternal.class), entity);
				val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
				val targetMachine = buildVariant.getAxisValue(TARGET_MACHINE_COORDINATE_AXIS);

				if (project.getPlugins().stream().anyMatch(appliedPlugin -> isNativeLanguagePlugin(appliedPlugin))) {
					taskRegistry.register(TaskIdentifier.of(TaskName.of("objects"), ObjectsLifecycleTask.class, variantIdentifier), configureDependsOn(knownVariant.map(it -> it.getSharedLibrary().getCompileTasks())));
				}

				taskRegistry.register(TaskIdentifier.of(TaskName.of("sharedLibrary"), SharedLibraryLifecycleTask.class, variantIdentifier), configureDependsOn(knownVariant.map(it -> it.getSharedLibrary().getLinkTask())));

				if (targetMachines.size() > 1) {
					val jvmJarBinary = knownVariant.flatMap(variant -> variant.getBinaries().withType(DefaultJvmJarBinary.class).getElements()).orElse(ImmutableSet.of());
					taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), variantIdentifier)).configure(configureDependsOn(knownVariant.map(it -> it.getJar().getJarTask()), jvmJarBinary));
				}

				// Include native runtime files inside JNI jar
				if (targetMachines.size() == 1) {
					if (project.getPluginManager().hasPlugin("java")) {
						taskRegistry.registerIfAbsent(JavaPlugin.JAR_TASK_NAME, Jar.class, task -> {
							task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
							task.setDescription("Assembles a jar archive containing the main classes and shared library.");
						}).configure(configureJarTaskUsing(knownVariant, unbuildableMainComponentLogger));

						// NOTE: We don't need to attach the JNI JAR to runtimeElements as the `java` plugin take cares of this.
					} else {
						TaskProvider<Jar> jarTask = taskRegistry.register(JavaPlugin.JAR_TASK_NAME, Jar.class, task -> {
							task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
							task.setDescription("Assembles a jar archive containing the shared library.");
						});
						jarTask.configure(configureJarTaskUsing(knownVariant, unbuildableMainComponentLogger));

						// Attach JNI Jar to runtimeElements
						// TODO: We could set the classes directory as secondary variant.
						// TODO: We could maybe set the shared library directory as secondary variant.
						//  However, the shared library would requires the resource path to be taken into consideration...
						getConfigurations().named("runtimeElements", it -> it.getOutgoing().artifact(jarTask.flatMap(Jar::getArchiveFile)));
					}
				} else {
					TaskProvider<Jar> jarTask = taskRegistry.register(TaskIdentifier.of(TaskName.of(JavaPlugin.JAR_TASK_NAME), Jar.class, variantIdentifier), task -> {
						configureJarTaskUsing(knownVariant, unbuildableMainComponentLogger).execute(task);

						val archiveBaseName = BaseNameUtils.from(variantIdentifier).getAsString() + variantIdentifier.getAmbiguousDimensions().getAsKebabCase().map(it -> "-" + it).orElse("");
						task.getArchiveBaseName().set(archiveBaseName);
					});

					// Attach JNI Jar to runtimeElements
					// TODO: only for the buildable elements? For a single variant, we attach the JNI binaries (see above)...
					//  for multiple one, it's a bit convoluted.
					//  If a buildable variant is available, we can attach that one and everything will be ketchup.
					//  However, if all variants are unbuildable, we should still be alright as the consumer will still crash, but because of not found... :-(
					//  We should probably attach at least one of the unbuildable variant to give a better error message.
					// TODO: We should really be testing: toolChainSelector.canBuild(targetMachineInternal)
					//  However, since we have to differ everything for testing, we have to approximate the API.
					if (toolChainSelectorInternal.canBuild(targetMachine)) {
						// TODO: We could maybe set the shared library directory as secondary variant.
						//  However, the shared library would requires the resource path to be taken into consideration...
						getConfigurations().named("runtimeElements", it -> it.getOutgoing().artifact(jarTask.flatMap(Jar::getArchiveFile)));
					}
				}


				// Attach JNI Jar to assemble task
				if (TargetMachines.isTargetingHost(targetMachine)) {
					// Attach JNI Jar to assemble
					taskRegistry.registerIfAbsent(ASSEMBLE_TASK_NAME).configure(it -> {
						it.dependsOn(knownVariant.map(l -> l.getJar().getJarTask()));
					});
				}
			}));
		});

		project.afterEvaluate(proj -> {
			// Ensure the variants are resolved so all tasks are registered.
			getTasks().named("tasks", task -> {
				task.dependsOn((Callable) () -> {
					ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
					return emptyList();
				});
			});
			// Ensure the variants are resolved so all configurations and dependencies are registered.
			getTasks().named("dependencies", task -> {
				task.dependsOn((Callable) () -> {
					ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
					return emptyList();
				});
			});
			getTasks().named("outgoingVariants", task -> {
				task.dependsOn((Callable) () -> {
					ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
					return emptyList();
				});
			});
		});
		// Differ this rules until after the project is evaluated to avoid interfering with other plugins
		project.afterEvaluate(proj -> {
			// The previous trick doesn't work for dependencyInsight task and vice-versa.
			project.getConfigurations().addRule("Java Native Interface (JNI) variants are resolved only when needed.", it -> {
				ModelProperties.getProperty(ModelNodes.of(extension), "variants").as(VariantView.class).get();
			});
		});
		project.afterEvaluate(finalizeModelNodeOf(extension));
	}

	private static void whenElementKnown(Object target, ModelAction action) {
		applyTo(ModelNodes.of(target), allDirectDescendants(stateAtLeast(ModelState.Created)).apply(once(action)));
	}

	private static boolean isNativeLanguagePlugin(Plugin<Project> appliedPlugin) {
		return NATIVE_LANGUAGE_PLUGINS.stream().anyMatch(it -> it.isAssignableFrom(appliedPlugin.getClass()));
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

	private Action<Jar> configureJarTaskUsing(KnownDomainObject<JniLibraryInternal> library, WarnUnbuildableLogger logger) {
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
			task.from(library.map(it -> {
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
						return it.getNativeRuntimeFiles();
					} else {
						runnableLogger.run();
						return emptyList();
					}
				}
				return it.getNativeRuntimeFiles();
			}), spec -> {
				// Don't resolve the resourcePath now as the JVM Kotlin plugin (as of 1.3.72) was resolving the `jar` task early.
				spec.into(library.map(JniLibraryInternal::getResourcePath));
			});
		};
	}

	private DefaultJvmJarBinary createJvmBinary(Project project) {
		TaskProvider<Jar> jvmJarTask = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class);
		return new DefaultJvmJarBinary(jvmJarTask);
	}

	private JavaNativeInterfaceLibrary registerExtension(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		project.getPluginManager().apply(JvmLanguageBasePlugin.class);

		val componentProvider = project.getExtensions().getByType(ModelRegistry.class).register(javaNativeInterfaceLibrary("main", project)).as(JavaNativeInterfaceLibrary.class);
		componentProvider.configure(configureUsingProjection(JniLibraryComponentInternal.class, baseNameConvention(project.getName())));
		val library = componentProvider.get();

		val dependencies = library.getDependencies();

		project.getPluginManager().withPlugin("java", appliedPlugin -> {
			getConfigurations().getByName("runtimeOnly").extendsFrom(dependencies.getJvmRuntimeOnly().getAsConfiguration());
		});

		project.getExtensions().add(JavaNativeInterfaceLibrary.class, "library", library);
		return library;
	}

	public static ModelRegistration javaNativeInterfaceLibrary(String name, Project project) {
		val identifier = ComponentIdentifier.builder().name(ComponentName.of(name)).displayName("JNI library").withProjectIdentifier(ProjectIdentifier.of(project)).build();
		val entityPath = ModelPath.path(name);
		return ModelRegistration.builder()
			.withComponent(entityPath)
			.withComponent(identifier)
			.withComponent(createdUsing(of(JavaNativeInterfaceLibrary.class), () -> project.getObjects().newInstance(DefaultJavaNativeInterfaceLibrary.class)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(LanguageSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.withComponent(IsComponent.tag())
			.withComponent(createdUsing(of(JniLibraryComponentInternal.class), () -> new JniLibraryComponentInternal(identifier, GroupId.of(project::getGroup), project.getObjects(), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskRegistry.class))))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPath, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				public void execute(ModelNode entity, ModelPath path, ModelState state) {
					if (entityPath.equals(path) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

						registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "jni")));

						// TODO: ONLY if applying include language plugin
						registry.register(project.getExtensions().getByType(CHeaderSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "headers")));

						registry.register(project.getExtensions().getByType(ComponentSourcesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "sources"), JavaNativeInterfaceLibrarySources.class));

						project.getPluginManager().withPlugin("groovy", ignored -> {
							registry.register(project.getExtensions().getByType(GroovySourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "groovy")));
						});
						project.getPluginManager().withPlugin("java", ignored -> {
							registry.register(project.getExtensions().getByType(JavaSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "java")));
						});
						project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", ignored -> {
							registry.register(project.getExtensions().getByType(KotlinSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(identifier, "kotlin")));
						});

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
						registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "dependencies"), JavaNativeInterfaceLibraryComponentDependencies.class, ModelBackedJavaNativeInterfaceLibraryComponentDependencies::new));
						val api = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("api"), identifier)));
						val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmImplementation"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("jvmRuntimeOnly"), identifier)));
						project.getPlugins().withType(NativeLanguagePlugin.class, new Action<NativeLanguagePlugin>() {
							private boolean alreadyExecuted = false;

							@Override
							public void execute(NativeLanguagePlugin appliedPlugin) {
								if (!alreadyExecuted) {
									alreadyExecuted = true;
									registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeCompileOnly"), identifier)));
								}
							}
						});
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeImplementation"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeLinkOnly"), identifier)));
						registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeRuntimeOnly"), identifier)));
						implementation.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

						val consumableFactory = project.getExtensions().getByType(ConsumableDependencyBucketRegistrationFactory.class);
						val apiElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("apiElements"), identifier)));
						apiElements.configure(Configuration.class, configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_API)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))));
						apiElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));
						val runtimeElements = registry.register(consumableFactory.create(DependencyBucketIdentifier.of(consumable("runtimeElements"), identifier)));
						runtimeElements.configure(Configuration.class, configureAttributes(builder -> builder.usage(project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)).attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR))));
						runtimeElements.configure(Configuration.class, configureExtendsFrom(api.as(Configuration.class)));

						registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "variants"), JniLibrary.class));

						registry.register(project.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "tasks")));

						val assembleTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier), Task.class).build());
						assembleTask.configure(Task.class, configureBuildGroup());
						assembleTask.configure(Task.class, configureDescription("Assembles the outputs of %s.", identifier));

						registry.register(ModelRegistration.builder()
							.withComponent(path.child("binaries"))
							.withComponent(IsModelProperty.tag())
							.withComponent(createdUsing(of(BinaryView.class), () -> {
								return ModelNodeUtils.get(entity, JniLibraryComponentInternal.class).getBinaries();
							}))
							.build());

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						val buildVariants = entity.addComponent(new BuildVariants(entity, project.getProviders(), project.getObjects()));
						val toolChainSelectorInternal = project.getObjects().newInstance(ToolChainSelectorInternal.class);
						registry.register(dimensions.newAxisProperty(path.child("targetMachines"))
							.axis(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(TargetMachines.host())
							.validateUsing((Iterable<TargetMachine> it) -> assertTargetMachinesAreKnown(it, toolChainSelectorInternal))
							.build());
						registry.register(dimensions.newAxisProperty(path.child("targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.SHARED)
							.build());
						registry.register(dimensions.buildVariants(path.child("buildVariants"), buildVariants.get()));
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ComponentIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, id, ignored) -> {
				if (id.equals(identifier)) {
					val component = ModelNodeUtils.get(entity, JniLibraryComponentInternal.class);
					component.getDevelopmentVariant().convention(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(component.getVariants()::get)));

					component.finalizeValue();

					val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
					component.getBuildVariants().get().forEach(buildVariant -> {
						val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(JniLibraryInternal.class).build();
						val variant = project.getExtensions().getByType(ModelRegistry.class).register(javaNativeInterfaceLibraryVariant(variantIdentifier, component, project));

						ModelStates.realize(ModelNodes.of(variant)); // FIXME: Remove once the refactoring is over

						variants.put(buildVariant, ModelNodes.of(variant));
					});
					entity.addComponent(new Variants(variants.build()));
				}
			}))

			// TODO: This convention is only good is to support older Gradle style source set
			//   We could deprecate this behaviour by creating additional source set named objc/objcpp and warn when there's sources in them... that would fail some tests because we don't expect a source set... we could also just remove it...
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(ObjectiveCSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName()), defaultObjectiveCGradle(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.ofAny(projectionOf(ObjectiveCppSourceSet.class)), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, projection, ignored) -> {
				if (entityPath.isDescendant(path)) {
					withConventionOf(maven(identifier.getName()), defaultObjectiveCppGradle(identifier.getName())).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.build()
			;
	}

	private static void assertTargetMachinesAreKnown(Iterable<TargetMachine> targetMachines, ToolChainSelectorInternal toolChainSelector) {
		List<TargetMachine> unknownTargetMachines = Streams.stream(targetMachines).filter(it -> !toolChainSelector.isKnown(it)).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + it.getOperatingSystemFamily().getCanonicalName() + " " + it.getArchitecture().getCanonicalName()).collect(joining("\n")));
		}
	}

	private static ModelRegistration javaNativeInterfaceLibraryVariant(VariantIdentifier<JniLibraryInternal> identifier, JniLibraryComponentInternal component, Project project) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		Preconditions.checkArgument(buildVariant.hasAxisValue(TARGET_MACHINE_COORDINATE_AXIS));

		return ModelRegistration.builder()
			.withComponent(DomainObjectIdentifierUtils.toPath(identifier))
			.withComponent(IsVariant.tag())
			.withComponent(identifier)
			.withComponent(createdUsing(of(JniLibraryInternal.class), () -> project.getObjects().newInstance(JniLibraryInternal.class, identifier, component.getSources(), component.getGroupId(), project.getObjects(), project.getConfigurations(), project.getProviders(), project.getExtensions().getByType(TaskRegistry.class), project.getExtensions().getByType(DomainObjectEventPublisher.class), project.getExtensions().getByType(BinaryViewFactory.class), project.getExtensions().getByType(TaskViewFactory.class))))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), (entity, path, id, ignored) -> {
				if (id.equals(identifier)) {
					entity.addComponent(new ModelBackedNativeIncomingDependencies(path, project.getObjects(), project.getProviders(), project.getExtensions().getByType(ModelLookup.class), s -> "native" + capitalize(s)));
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(VariantIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), ModelComponentReference.of(ModelPath.class), (entity, id, ignored, path) -> {
				if (id.equals(identifier)) {
					val registry = project.getExtensions().getByType(ModelRegistry.class);
					val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));
					registry.register(project.getExtensions().getByType(ComponentDependenciesPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(identifier, "dependencies"), JavaNativeInterfaceNativeComponentDependencies.class, ModelBackedJavaNativeInterfaceNativeComponentDependencies::new));
					val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeImplementation"), identifier)));
					val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeLinkOnly"), identifier)));
					val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeRuntimeOnly"), identifier)));

					val resolvableFactory = project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class);
					project.getPlugins().withType(NativeLanguagePlugin.class, new Action<NativeLanguagePlugin>() {
						private boolean alreadyExecuted = false;

						@Override
						public void execute(NativeLanguagePlugin appliedPlugin) {
							if (!alreadyExecuted) {
								alreadyExecuted = true;
								val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("nativeCompileOnly"), identifier)));
								val headerSearchPaths = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("nativeHeaderSearchPaths"), identifier)));
								headerSearchPaths.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), compileOnly.as(Configuration.class))
									.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API))))
									.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
									.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
							}
						}
					});
					val linkLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("nativeLinkLibraries"), identifier)));
					linkLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), linkOnly.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_LINK))))
						.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));
					val runtimeLibraries = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("nativeRuntimeLibraries"), identifier)));
					runtimeLibraries.configure(Configuration.class, configureExtendsFrom(implementation.as(Configuration.class), runtimeOnly.as(Configuration.class))
						.andThen(configureAttributes(it -> it.usage(project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME))))
						.andThen(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) identifier.getBuildVariant(), project.getObjects()))
						.andThen(ConfigurationUtilsEx::configureAsGradleDebugCompatible));

					val assembleTask = registry.register(project.getExtensions().getByType(TaskRegistrationFactory.class).create(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), identifier), Task.class).build());
					assembleTask.configure(Task.class, task -> {
						task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
						task.setDescription(String.format("Assembles the '%s' outputs of this project.", BuildVariantNamer.INSTANCE.determineName((BuildVariantInternal) identifier.getBuildVariant())));
					});
					registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).create(ModelPropertyIdentifier.of(id, "assembleTask"), ModelNodes.of(assembleTask)));

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

	public static abstract class DefaultJavaNativeInterfaceLibrary implements JavaNativeInterfaceLibrary
		, ModelBackedDependencyAwareComponentMixIn<JavaNativeInterfaceLibraryComponentDependencies>
		, ModelBackedVariantAwareComponentMixIn<JniLibrary>
		, ModelBackedSourceAwareComponentMixIn<JavaNativeInterfaceLibrarySources>
		, ModelBackedBinaryAwareComponentMixIn
		, ModelBackedTaskAwareComponentMixIn
		, ModelBackedNamedMixIn
	{
	}

	private static boolean isGradleVersionGreaterOrEqualsTo6Dot3() {
		return GradleVersion.current().compareTo(GradleVersion.version("6.3")) >= 0;
	}

	private void registerJvmHeaderSourceSet(JavaNativeInterfaceLibrary extension) {
		// TODO: This is an external dependency meaning we should go through the component dependencies.
		//  We can either add an file dependency or use the, yet-to-be-implemented, shim to consume system libraries
		//  We aren't using a language source set as the files will be included inside the IDE projects which is not what we want.
		extension.getBinaries().configureEach(BaseNativeBinary.class, binary -> {
			binary.getCompileTasks().configureEach(NativeSourceCompileTask.class, includeRoots(from(jvmIncludes())));
		});
	}

	private void registerJniHeaderSourceSet(Project project, JavaNativeInterfaceLibrary extension) {
		SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
		org.gradle.api.tasks.SourceSet main = sourceSets.getByName("main");

		TaskProvider<JavaCompile> compileTask = project.getTasks().named(main.getCompileJavaTaskName(), JavaCompile.class, task -> {
			task.getOptions().getHeaderOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("generated/jni-headers"));

			// The nested output is not marked automatically as an output of the task regarding task dependencies.
			// So we mark it manually here.
			// See https://github.com/gradle/gradle/issues/6619.
			if (!isGradleVersionGreaterOrEqualsTo6Dot3()) {
				task.getOutputs().dir(task.getOptions().getHeaderOutputDirectory());
			}

			// Cannot do incremental header generation before 6.3, since the pattern for cleaning them up is currently wrong.
			// See https://github.com/gradle/gradle/issues/12084.
			task.getOptions().setIncremental(isGradleVersionGreaterOrEqualsTo6Dot3());
		});
		extension.getSources().configure("jni", CHeaderSet.class, sourceSet -> {
			sourceSet.from(compileTask.flatMap(it -> it.getOptions().getHeaderOutputDirectory()));
		});
	}

	private void configureJavaJniRuntime(Project project, JavaNativeInterfaceLibrary library) {
		val projection = ModelNodeUtils.get(ModelNodes.of(library), JniLibraryComponentInternal.class);

		// Wire JVM to JniLibrary
		project.getConfigurations().getByName("implementation").extendsFrom(projection.getJvmImplementationDependencies());

		project.getTasks().named("test", Test.class, task -> {
			Provider<List<FileCollection>> files = library.getVariants().map(JniLibrary::getNativeRuntimeFiles);
			task.dependsOn((Callable<Iterable<File>>)() -> {
				val variant = projection.getDevelopmentVariant().getOrNull();
				if (variant == null) {
					return emptyList();
				}
				return variant.getNativeRuntimeFiles();
			});

			// TODO: notify when no native library exists
			task.getJvmArgumentProviders().add(new CommandLineArgumentProvider() {
				@Override
				public Iterable<String> asArguments() {
					String path = files.get().stream().flatMap(it -> it.getFiles().stream()).map(it -> it.getParentFile().getAbsolutePath()).collect(joining(File.pathSeparator));
					return ImmutableList.of("-Djava.library.path=" + path);
				}
			});
		});
	}

	/**
	 * Consider the following guidelines when writing the error message:
	 *  - Clear, short and meaningful
	 *  - No jargon
	 *  - Be humble (no blaming)
	 *  - Provide action
	 *  - Yes answers
	 */
	static abstract class IncompatiblePluginsAdvice {
		static final Set<String> SOFTWARE_MODEL_PLUGIN_IDS = ImmutableSet.of("cpp", "cpp-lang", "c", "c-lang", "objective-c", "objective-c-lang", "objective-cpp", "objective-cpp-lang");
		static final Set<String> CURRENT_MODEL_PLUGIN_IDS = ImmutableSet.of("cpp-library", "cpp-application", "swift-library", "swift-application");
		static final String JAVA_APPLICATION_PLUGIN_ID = "application";
		static final String JAVA_LIBRARY_PLUGIN_ID = "java-library";

		private static final String SOFTWARE_MODEL_MIGRATION = "To learn more about software model migration, visit https://nokee.dev/docs/migrating-from-software-model";
		private static final String CURRENT_MODEL_MIGRATION = "To learn more about Gradle core native plugin migration, visit https://nokee.dev/docs/migrating-from-core-plugins";
		private static final String PROJECT_ENTRY_POINT = "To learn more about project entry points, visit https://nokee.dev/docs/project-entry-points";
		private static final String LEARN_CPP_LANGUAGE = "To learn more about 'dev.nokee.cpp-language' plugin, visit https://nokee.dev/docs/cpp-language-plugin";
		private static final String USE_CPP_LANGUAGE = "Use 'dev.nokee.cpp-language' plugin instead of the 'cpp-application' and 'cpp-library' plugins";
		private static final String VOTE_SWIFT_LANGUAGE = "Vote on https://github.com/nokeedev/gradle-native/issues/26 issue to show interest for Swift language support";
		private static final String REMOTE_SWIFT_PLUGINS = "Remove 'swift-application' and 'swift-library' plugins from the project";

		static void forJavaApplicationEntryPointPlugin(String pluginId, IncompatiblePluginUsage.Context context) {
			context.advice("Refer to https://nokee.dev/docs/building-jni-application for learning how to build JNI application")
				.withFootnote(PROJECT_ENTRY_POINT);
		}

		static void forJavaLibraryEntryPointPlugin(String pluginId, IncompatiblePluginUsage.Context context) {
			context.advice("Use 'java' plugin instead of 'java-library' plugin")
				.withFootnote(PROJECT_ENTRY_POINT);
		}

		static void forSoftwareModelNativePlugins(String pluginId, IncompatiblePluginUsage.Context context) {
			switch (pluginId) {
				case "cpp":
				case "cpp-lang":
					context.advice("Use 'dev.nokee.cpp-language' plugin instead of the 'cpp' and 'cpp-lang' plugins")
						.withFootnote(LEARN_CPP_LANGUAGE)
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
				case "c":
				case "c-lang":
					context.advice("Use 'dev.nokee.c-language' plugin instead of the 'c' and 'c-lang' plugins")
						.withFootnote("To learn more about 'dev.nokee.c-language' plugin, visit https://nokee.dev/docs/c-language-plugin")
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
				case "objective-c":
				case "objective-c-lang":
					context.advice("Use 'dev.nokee.objective-c-language' plugin instead of the 'objective-c' and 'objective-c-lang' plugins")
						.withFootnote("To learn more about 'dev.nokee.objective-c-language' plugin, visit https://nokee.dev/docs/objective-c-language-plugin")
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
				case "objective-cpp":
				case "objective-cpp-lang":
					context.advice("Use 'dev.nokee.objective-cpp-language' plugin instead of the 'objective-cpp' and 'objective-cpp-lang' plugins")
						.withFootnote("To learn more about 'dev.nokee.objective-cpp-language' plugin, visit https://nokee.dev/docs/objective-cpp-language-plugin")
						.withFootnote(SOFTWARE_MODEL_MIGRATION);
					break;
			}
		}

		static void forCurrentModelNativePlugins(String pluginId, IncompatiblePluginUsage.Context context) {
			switch (pluginId) {
				case "cpp-application":
				case "cpp-library":
					context.advice(USE_CPP_LANGUAGE)
						.withFootnote(LEARN_CPP_LANGUAGE)
						.withFootnote(PROJECT_ENTRY_POINT)
						.withFootnote(CURRENT_MODEL_MIGRATION);
					break;
				case "swift-application":
				case "swift-library":
					context.advice(REMOTE_SWIFT_PLUGINS)
						.withFootnote(PROJECT_ENTRY_POINT);
					context.advice(VOTE_SWIFT_LANGUAGE);
					break;
			}
		}

		static void forNativeBasePlugin(String pluginId, IncompatiblePluginUsage.Context context) {
			context.advice(USE_CPP_LANGUAGE)
				.withFootnote(LEARN_CPP_LANGUAGE)
				.withFootnote(PROJECT_ENTRY_POINT)
				.withFootnote(CURRENT_MODEL_MIGRATION);
			context.advice(REMOTE_SWIFT_PLUGINS)
				.withFootnote(PROJECT_ENTRY_POINT);
			context.advice(VOTE_SWIFT_LANGUAGE);
		}
	}
}
