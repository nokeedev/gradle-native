package dev.nokee.platform.jni.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.c.internal.CSourceSetTransform;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSetTransform;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetTransform;
import dev.nokee.language.objectivec.internal.UTTypeObjectiveCSource;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetTransform;
import dev.nokee.language.objectivecpp.internal.UTTypeObjectiveCppSource;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.NamingSchemeFactory;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.*;
import dev.nokee.runtime.nativebase.internal.*;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinFrameworkResolutionSupportPlugin;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.nativeplatform.internal.toolchains.ToolChainSelector;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.plugins.NativeBasePlugin;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.nokee.platform.jni.internal.plugins.JniLibraryPlugin.IncompatiblePluginsAdvice.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

public abstract class JniLibraryPlugin implements Plugin<Project> {
	private static final String EXTENSION_NAME = "library";

	private final ToolChainSelectorInternal toolChainSelector = getObjects().newInstance(ToolChainSelectorInternal.class);

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ProjectLayout getLayout();

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
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		NamingSchemeFactory namingSchemeFactory = new NamingSchemeFactory(project.getName());
		NamingScheme mainComponentNames = namingSchemeFactory.forMainComponent();
		JniLibraryExtensionInternal extension = registerExtension(project, mainComponentNames);
		project.afterEvaluate(getObjects().newInstance(TargetMachineRule.class, extension.getTargetMachines(), EXTENSION_NAME));

		// TODO: On `java` apply, just apply the `java-library` (but don't allow other users to apply it
		project.getPluginManager().withPlugin("java", appliedPlugin -> configureJavaJniRuntime(project, extension));
		project.getPluginManager().withPlugin("java", appliedPlugin -> registerJniHeaderSourceSet(project, extension));
		project.getPlugins().withType(NativePlatformCapabilitiesMarkerPlugin.class, appliedPlugin -> {
			project.getPluginManager().apply(DarwinFrameworkResolutionSupportPlugin.class);
		});

		project.afterEvaluate(proj -> {
			Set<TargetMachine> targetMachines = extension.getTargetMachines().get();
			Optional<DefaultJvmJarBinary> jvmJarBinary = findJvmBinary(proj);
			extension.getBuildVariants().get().forEach(buildVariant -> {
				final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine((DefaultOperatingSystemFamily)buildVariant.getDimensions().get(0), (DefaultMachineArchitecture)buildVariant.getDimensions().get(1));
				final NamingScheme names = mainComponentNames.forBuildVariant(buildVariant, extension.getBuildVariants().get());

				// Find toolchain capable of building C++
				final NamedDomainObjectProvider<JniLibraryInternal> library = extension.getVariantCollection().registerVariant(buildVariant, it -> {
					// Build all language source set
					List<SourceSet<UTTypeObjectCode>> objectSourceSets = new ArrayList<>();
					if (project.getPlugins().hasPlugin(NativePlatformCapabilitiesMarkerPlugin.class)) {
						ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
						Configuration compileConfiguration = getConfigurations().create(names.getConfigurationName("headerSearchPaths"), configurationUtils.asIncomingHeaderSearchPathFrom(extension.getNativeImplementationDependencies()));

						if (proj.getPluginManager().hasPlugin("dev.nokee.cpp-language")) {
							SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(CppSourceSet.class).srcDir("src/main/cpp").transform(getObjects().newInstance(CppSourceSetTransform.class, names, compileConfiguration));
							objectSourceSets.add(objectSourceSet);
						}
						if (proj.getPluginManager().hasPlugin("dev.nokee.c-language")) {
							SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(CSourceSet.class).srcDir("src/main/c").transform(getObjects().newInstance(CSourceSetTransform.class, names, compileConfiguration));
							objectSourceSets.add(objectSourceSet);
						}
						if (proj.getPluginManager().hasPlugin("dev.nokee.objective-cpp-language")) {
							SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCppSource()).srcDir("src/main/objcpp").transform(getObjects().newInstance(ObjectiveCppSourceSetTransform.class, names, compileConfiguration));
							objectSourceSets.add(objectSourceSet);
						}
						if (proj.getPluginManager().hasPlugin("dev.nokee.objective-c-language")) {
							SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCSource()).srcDir("src/main/objc").transform(getObjects().newInstance(ObjectiveCSourceSetTransform.class, names, compileConfiguration));
							objectSourceSets.add(objectSourceSet);
						}

						objectSourceSets.forEach(objects -> {
							GeneratedSourceSet<?> source = (GeneratedSourceSet<?>)objects;
							source.getGeneratedByTask().configure(task -> {
								AbstractNativeCompileTask compileTask = (AbstractNativeCompileTask)task;

								NativePlatformFactory nativePlatformFactory = new NativePlatformFactory();
								NativePlatformInternal nativePlatform = nativePlatformFactory.create(targetMachineInternal);
								compileTask.getTargetPlatform().set(nativePlatform);
								compileTask.getTargetPlatform().finalizeValueOnRead();
								compileTask.getTargetPlatform().disallowChanges();

								NativeToolChainInternal toolChain = toolChainSelector.select(targetMachineInternal);
								compileTask.getToolChain().set(toolChain);
								compileTask.getToolChain().finalizeValueOnRead();
								compileTask.getToolChain().disallowChanges();

								final Supplier<ToolType> toolTypeSupplier = () -> {
									ToolType toolType = null;
									if (compileTask instanceof CCompileTask) {
										toolType = ToolType.CPP_COMPILER;
									} else if (compileTask instanceof CppCompileTask) {
										toolType = ToolType.CPP_COMPILER;
									} else if (compileTask instanceof ObjectiveCCompileTask) {
										toolType = ToolType.OBJECTIVEC_COMPILER;
									} else if (compileTask instanceof ObjectiveCppCompileTask) {
										toolType = ToolType.OBJECTIVECPP_COMPILER;
									}
									return toolType;
								};

								final Callable<List<File>> systemIncludes = () -> toolChain.select(nativePlatform).getSystemLibraries(toolTypeSupplier.get()).getIncludeDirs();
								compileTask.getSystemIncludes().from(systemIncludes);
							});
						});
					}

					TaskProvider<LinkSharedLibraryTask> linkTask = getTasks().register(names.getTaskName("link"), LinkSharedLibraryTask.class, task -> {
						task.setDescription("Links the shared library.");
						objectSourceSets.stream().map(SourceSet::getAsFileTree).forEach(task::source);

						NativePlatformFactory nativePlatformFactory = new NativePlatformFactory();
						NativePlatformInternal nativePlatform = nativePlatformFactory.create(targetMachineInternal);
						task.getTargetPlatform().set(nativePlatform);
						task.getTargetPlatform().finalizeValueOnRead();
						task.getTargetPlatform().disallowChanges();

						// Until we model the build type
						task.getDebuggable().set(false);

						// Install name set inside SharedLibraryBinaryInternal

						task.getDestinationDirectory().convention(getLayout().getBuildDirectory().dir(names.getOutputDirectoryBase("libs")));
						task.getLinkedFile().convention(getLayout().getBuildDirectory().file(nativePlatform.getOperatingSystem().getInternalOs().getSharedLibraryName(names.getOutputDirectoryBase("libs") + "/" + project.getName())));

						task.getToolChain().set(getProviders().provider(() -> toolChainSelector.select(targetMachineInternal)));
						task.getToolChain().finalizeValueOnRead();
						task.getToolChain().disallowChanges();

						// For windows
						task.getImportLibrary().set(getProviders().provider(() -> {
							PlatformToolProvider toolProvider = ((NativeToolChainInternal)task.getToolChain().get()).select(nativePlatform);
							if (toolProvider.producesImportLibrary()) {
								return getLayout().getBuildDirectory().file(toolProvider.getImportLibraryName(names.getOutputDirectoryBase("libs") + "/" + project.getName())).get();
							}
							return null;
						}));
					});

					it.registerSharedLibraryBinary(objectSourceSets.stream().map(s -> (GeneratedSourceSet<UTTypeObjectCode>)s).collect(Collectors.toList()), linkTask, targetMachines.size() > 1);

					if (jvmJarBinary.isPresent() && targetMachines.size() == 1) {
						it.addJniJarBinary(jvmJarBinary.get());
					} else {
						it.registerJniJarBinary();
						jvmJarBinary.ifPresent(it::addJvmJarBinary);
//					if (proj.getPluginManager().hasPlugin("java")) {
//						library.getAssembleTask().configure(task -> task.dependsOn(project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class)));
//					} else {
//						// FIXME: There is a gap here, if the project doesn't have any JVM plugin applied but specify multiple target machine what is expected?
//						//   Only JNI Jar? or an empty JVM Jar and JNI Jar?... Hmmm....
//					}
					}
				});

				if (project.getPlugins().hasPlugin(NativePlatformCapabilitiesMarkerPlugin.class)) {
					getTasks().register(names.getTaskName("objects"), task -> {
						task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
						task.setDescription("Assembles main objects.");
						task.dependsOn(library.map(it -> it.getSharedLibrary().getCompileTasks()));
					});
				}

				getTasks().register(names.getTaskName("sharedLibrary"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles a shared library binary containing the main objects.");
					task.dependsOn(library.map(it -> it.getSharedLibrary().getLinkTask()));
				});

				if (targetMachines.size() > 1) {
					getTasks().register(names.getTaskName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME), task -> {
						task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
						task.setDescription(String.format("Assembles the '%s' outputs of this project.", library.getName()));
						task.dependsOn(library.map(it -> it.getSharedLibrary().getLinkTask()));
						task.dependsOn(library.map(it -> it.getJar().getJarTask()));
						task.dependsOn(jvmJarBinary.map(it -> ImmutableList.of(it.getJarTask())).orElse(ImmutableList.of()));
					});
				}

				// Include native runtime files inside JNI jar
				if (targetMachines.size() == 1) {
					if (project.getPluginManager().hasPlugin("java")) {
						getTasks().named("jar", Jar.class, task -> {
							task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
							task.setDescription("Assembles a jar archive containing the main classes and shared library.");
							configureJarTaskUsing(library).execute(task);
						});

						// NOTE: We don't need to attach the JNI JAR to runtimeElements as the `java` plugin take cares of this.
					} else {
						TaskProvider<Jar> jarTask = getTasks().register(JavaPlugin.JAR_TASK_NAME, Jar.class, task -> {
							task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
							task.setDescription("Assembles a jar archive containing the shared library.");
							configureJarTaskUsing(library).execute(task);
						});

						// Attach JNI Jar to runtimeElements
						// TODO: We could set the classes directory as secondary variant.
						// TODO: We could maybe set the shared library directory as secondary variant.
						//  However, the shared library would requires the resource path to be taken into consideration...
						getConfigurations().named("runtimeElements", it -> it.getOutgoing().artifact(jarTask.flatMap(Jar::getArchiveFile)));
					}
				} else {
					TaskProvider<Jar> jarTask = getTasks().register(names.getTaskName("jar"), Jar.class, task -> {
						configureJarTaskUsing(library).execute(task);
						task.getArchiveBaseName().set(names.getBaseName().withKababDimensions());
					});

					// Attach JNI Jar to runtimeElements
					// TODO: only for the buildable elements? For a single variant, we attach the JNI binaries (see above)...
					//  for multiple one, it's a bit convoluted.
					//  If a buildable variant is available, we can attach that one and everything will be ketchup.
					//  However, if all variants are unbuildable, we should still be alright as the consumer will still crash, but because of not found... :-(
					//  We should probably attach at least one of the unbuildable variant to give a better error message.
					// TODO: We should really be testing: toolChainSelector.canBuild(targetMachineInternal)
					//  However, since we have to differ everything for testing, we have to approximate the API.
					if (toolChainSelector.canBuild(targetMachineInternal)) {
						// TODO: We could maybe set the shared library directory as secondary variant.
						//  However, the shared library would requires the resource path to be taken into consideration...
						getConfigurations().named("runtimeElements", it -> it.getOutgoing().artifact(jarTask.flatMap(Jar::getArchiveFile)));
					}
				}


				// Attach JNI Jar to assemble task
				if (DefaultTargetMachine.isTargetingHost().test(targetMachineInternal)) {
					// Attach JNI Jar to assemble
					project.getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, it -> {
						it.dependsOn(library.map(l -> l.getJar().getJarTask()));
					});
				}
			});

			extension.getVariantCollection().disallowChanges();
		});

		project.afterEvaluate(proj -> {
			// Ensure the variants are resolved so all tasks are registered.
			getTasks().named("tasks", task -> {
				task.dependsOn((Callable) () -> {
					extension.getVariantCollection().realize();
					return emptyList();
				});
			});
			// Ensure the variants are resolved so all configurations and dependencies are registered.
			getTasks().named("dependencies", task -> {
				task.dependsOn((Callable) () -> {
					extension.getVariantCollection().realize();
					return emptyList();
				});
			});
			getTasks().named("outgoingVariants", task -> {
				task.dependsOn((Callable) () -> {
					extension.getVariantCollection().realize();
					return emptyList();
				});
			});
		});
		// Differ this rules until after the project is evaluated to avoid interfering with other plugins
		project.afterEvaluate(proj -> {
			// The previous trick doesn't work for dependencyInsight task and vice-versa.
			project.getConfigurations().addRule("Java Native Interface (JNI) variants are resolved only when needed.", it -> {
				extension.getVariantCollection().realize();
			});
		});

		// Warn if component is cannot build on this machine
		getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, task -> {
			task.dependsOn((Callable) () -> {
				boolean targetsCurrentMachine = extension.getTargetMachines().get().stream().anyMatch(toolChainSelector::canBuild);
				if (!targetsCurrentMachine) {
					task.getLogger().warn("'main' component in project '" + project.getPath() + "' cannot build on this machine.");
				}
				return Collections.emptyList();
			});
		});
	}

	private Action<Jar> configureJarTaskUsing(Provider<JniLibraryInternal> library) {
		return task -> {
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
					if (it.getTargetMachine().getOperatingSystemFamily().equals(DefaultOperatingSystemFamily.HOST)) {
						return it.getNativeRuntimeFiles();
					} else {
						task.getLogger().warn("'main' component in project '" + task.getProject().getPath() + "' cannot build on this machine.");
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

	private Optional<DefaultJvmJarBinary> findJvmBinary(Project project) {
		if (project.getPluginManager().hasPlugin("java")) {
			TaskProvider<Jar> jvmJarTask = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class);
			return Optional.of(getObjects().newInstance(DefaultJvmJarBinary.class, jvmJarTask));
		}
		return Optional.empty();
	}

	@Inject
	protected abstract ToolChainSelector getToolChainSelector();

	@Inject
	protected abstract ObjectFactory getObjects();

	private static void assertNonEmpty(Collection<?> values, String propertyName, String componentName) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException(String.format("A %s needs to be specified for the %s.", propertyName, componentName));
		}
	}

	private void assertTargetMachinesAreKnown(Collection<TargetMachine> targetMachines) {
		List<TargetMachine> unknownTargetMachines = targetMachines.stream().filter(it -> !toolChainSelector.isKnown(it)).collect(Collectors.toList());
		if (!unknownTargetMachines.isEmpty()) {
			throw new IllegalArgumentException("The following target machines are not know by the defined tool chains:\n" + unknownTargetMachines.stream().map(it -> " * " + ((DefaultOperatingSystemFamily)it.getOperatingSystemFamily()).getName() + " " + ((DefaultMachineArchitecture)it.getArchitecture()).getName()).collect(joining("\n")));
		}
	}

	private JniLibraryExtensionInternal registerExtension(Project project, NamingScheme names) {
		JniLibraryDependenciesInternal dependencies = project.getObjects().newInstance(JniLibraryDependenciesInternal.class, names);
		Configuration jvmApiElements = Optional.ofNullable(project.getConfigurations().findByName("apiElements")).orElseGet(() -> {
			return project.getConfigurations().create("apiElements", configuration -> {
				ConfigurationUtils.configureAsOutgoing(configuration);
				configuration.setDescription("API elements for main.");
				configuration.attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API));
					attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR));
				});
			});
		});
		jvmApiElements.extendsFrom(dependencies.getApiDependencies());


		Configuration jvmRuntimeElements = Optional.ofNullable(project.getConfigurations().findByName("runtimeElements")).orElseGet(() -> {
			return project.getConfigurations().create("runtimeElements", configuration -> {
				ConfigurationUtils.configureAsOutgoing(configuration);
				configuration.setDescription("Elements of runtime for main.");
				configuration.attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
					attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR));
				});
			});
		});
		jvmRuntimeElements.extendsFrom(dependencies.getApiDependencies());

		project.getPluginManager().withPlugin("java", appliedPlugin -> {
			getConfigurations().getByName("runtimeOnly").extendsFrom(dependencies.getJvmRuntimeOnlyDependencies());
		});

		JniLibraryExtensionInternal library = project.getObjects().newInstance(JniLibraryExtensionInternal.class, dependencies, GroupId.of(project::getGroup), names);
		project.getExtensions().add(JniLibraryExtension.class, "library", library);
		return library;
	}

	private static boolean isGradleVersionGreaterOrEqualsTo6Dot3() {
		return GradleVersion.current().compareTo(GradleVersion.version("6.3")) >= 0;
	}

	private void registerJniHeaderSourceSet(Project project, JniLibraryExtensionInternal library) {
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
		HeaderExportingSourceSetInternal jniHeaderSourceSet = project.getObjects().newInstance(HeaderExportingSourceSetInternal.class);
		jniHeaderSourceSet.getSource().from(compileTask.flatMap(it -> it.getOptions().getHeaderOutputDirectory()));
		library.getSources().add(jniHeaderSourceSet);
	}

	private void configureJavaJniRuntime(Project project, JniLibraryExtensionInternal library) {
		// Wire JVM to JniLibrary
		project.getConfigurations().getByName("implementation").extendsFrom(library.getJvmImplementationDependencies());

		project.getTasks().named("test", Test.class, task -> {
			Provider<List<FileCollection>> files = getProviders().provider(() -> library.getVariantCollection().get().stream().map(it -> it.getNativeRuntimeFiles()).collect(Collectors.toList()));
			task.dependsOn(files);

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
