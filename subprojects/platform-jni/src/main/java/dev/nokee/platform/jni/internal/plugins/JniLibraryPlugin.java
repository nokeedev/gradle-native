package dev.nokee.platform.jni.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSet;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.language.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.*;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.dependencies.*;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinFrameworkResolutionSupportPlugin;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
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
import org.gradle.language.plugins.NativeBasePlugin;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
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

	private JniLibraryNativeDependenciesInternal newDependencies(NamingScheme names, BuildVariant buildVariant, JniLibraryComponentInternal component) {
		DefaultNativeComponentDependencies variantDependencies = component.getDependencies().getNativeDelegate();
		if (component.getBuildVariants().get().size() > 1) {
			variantDependencies = variantDependencies.extendsWith(names.withConfigurationNamePrefix("native"));
		}

		boolean hasSwift = !component.getSourceCollection().withType(SwiftSourceSet.class).isEmpty();
		boolean hasHeader = !component.getSourceCollection().matching(it -> it instanceof HeaderExportingSourceSet).isEmpty();
		SwiftModuleIncomingDependencies incomingSwiftDependencies = null;
		HeaderIncomingDependencies incomingHeaderDependencies = null;
		if (hasSwift) {
			incomingSwiftDependencies = getObjects().newInstance(DefaultSwiftModuleIncomingDependencies.class, names, variantDependencies);
			incomingHeaderDependencies = getObjects().newInstance(NoHeaderIncomingDependencies.class);
		} else if (hasHeader) {
			incomingHeaderDependencies = getObjects().newInstance(DefaultHeaderIncomingDependencies.class, names, variantDependencies, buildVariant);
			incomingSwiftDependencies = getObjects().newInstance(NoSwiftModuleIncomingDependencies.class);
		} else {
			incomingHeaderDependencies = getObjects().newInstance(NoHeaderIncomingDependencies.class);
			incomingSwiftDependencies = getObjects().newInstance(NoSwiftModuleIncomingDependencies.class);
		}

		NativeIncomingDependencies incoming = getObjects().newInstance(NativeIncomingDependencies.class, names.withConfigurationNamePrefix("native"), buildVariant, variantDependencies, incomingSwiftDependencies, incomingHeaderDependencies);

		return getObjects().newInstance(JniLibraryNativeDependenciesInternal.class, variantDependencies, incoming);
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

		PreparedLogger unbuildableMainComponentLogger = new OneTimeLogger(new WarnUnbuildableLogger(project.getPath()));
		project.afterEvaluate(proj -> {
			// Create source set on extension
			if (proj.getPluginManager().hasPlugin("dev.nokee.cpp-language")) {
				extension.getComponent().getSourceCollection().add(getObjects().newInstance(CppSourceSet.class).srcDir("src/main/cpp"));
			}
			if (proj.getPluginManager().hasPlugin("dev.nokee.c-language")) {
				extension.getComponent().getSourceCollection().add(getObjects().newInstance(CSourceSet.class).srcDir("src/main/c"));
			}
			if (proj.getPluginManager().hasPlugin("dev.nokee.objective-cpp-language")) {
				extension.getComponent().getSourceCollection().add(getObjects().newInstance(ObjectiveCppSourceSet.class).srcDir("src/main/objcpp"));
			}
			if (proj.getPluginManager().hasPlugin("dev.nokee.objective-c-language")) {
				extension.getComponent().getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class).srcDir("src/main/objc"));
			}

			Set<TargetMachine> targetMachines = extension.getTargetMachines().get();
			Optional<DefaultJvmJarBinary> jvmJarBinary = findJvmBinary(proj);
			extension.getBuildVariants().get().forEach(buildVariant -> {
				final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine((DefaultOperatingSystemFamily)buildVariant.getDimensions().get(0), (DefaultMachineArchitecture)buildVariant.getDimensions().get(1));
				final NamingScheme names = mainComponentNames.forBuildVariant(buildVariant, extension.getBuildVariants().get());

				JniLibraryNativeDependenciesInternal dependencies = newDependencies(names.withComponentDisplayName("JNI shared library"), buildVariant, extension.getComponent());
				final VariantProvider<JniLibraryInternal> library = extension.getVariantCollection().registerVariant(buildVariant, (name, bv) -> {
					JniLibraryInternal it = extension.getComponent().createVariant(name, bv, dependencies);

					// Build all language source set
					DomainObjectSet<GeneratedSourceSet> objectSourceSets = getObjects().domainObjectSet(GeneratedSourceSet.class);
					if (project.getPlugins().hasPlugin(NativePlatformCapabilitiesMarkerPlugin.class)) {
						objectSourceSets.addAll(getObjects().newInstance(NativeLanguageRules.class, names).apply(extension.getComponent().getSourceCollection()));
					}

					TaskProvider<LinkSharedLibraryTask> linkTask = getTasks().register(names.getTaskName("link"), LinkSharedLibraryTask.class);
					it.registerSharedLibraryBinary(objectSourceSets, linkTask, targetMachines.size() > 1, dependencies.getIncoming());

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

					return it;
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
						task.setDescription(String.format("Assembles the '%s' outputs of this project.", library.getDelegate().getName()));
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
							configureJarTaskUsing(library, unbuildableMainComponentLogger).execute(task);
						});

						// NOTE: We don't need to attach the JNI JAR to runtimeElements as the `java` plugin take cares of this.
					} else {
						TaskProvider<Jar> jarTask = getTasks().register(JavaPlugin.JAR_TASK_NAME, Jar.class, task -> {
							task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
							task.setDescription("Assembles a jar archive containing the shared library.");
							configureJarTaskUsing(library, unbuildableMainComponentLogger).execute(task);
						});

						// Attach JNI Jar to runtimeElements
						// TODO: We could set the classes directory as secondary variant.
						// TODO: We could maybe set the shared library directory as secondary variant.
						//  However, the shared library would requires the resource path to be taken into consideration...
						getConfigurations().named("runtimeElements", it -> it.getOutgoing().artifact(jarTask.flatMap(Jar::getArchiveFile)));
					}
				} else {
					TaskProvider<Jar> jarTask = getTasks().register(names.getTaskName("jar"), Jar.class, task -> {
						configureJarTaskUsing(library, unbuildableMainComponentLogger).execute(task);
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
					unbuildableMainComponentLogger.log();
				}
				return Collections.emptyList();
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

	private Action<Jar> configureJarTaskUsing(VariantProvider<JniLibraryInternal> library, PreparedLogger unbuildableMainComponentLogger) {
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
						File linkedFile = library.get().getSharedLibrary().getLinkTask().get().getLinkedFile().get().getAsFile();
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
					if (it.getTargetMachine().getOperatingSystemFamily().equals(DefaultOperatingSystemFamily.HOST)) {
						return it.getNativeRuntimeFiles();
					} else {
						unbuildableMainComponentLogger.log();
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
		JniLibraryExtensionInternal library = project.getObjects().newInstance(JniLibraryExtensionInternal.class, GroupId.of(project::getGroup), names);

		JniLibraryDependenciesInternal dependencies = library.getDependencies();

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
			Provider<List<? extends FileCollection>> files = library.getVariants().map(JniLibrary::getNativeRuntimeFiles);
			task.dependsOn((Callable<Iterable<File>>)() -> {
				val variant = library.getComponent().getDevelopmentVariant().getOrNull();
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

	private interface PreparedLogger {
		void log();
	}

	@RequiredArgsConstructor
	private static class WarnUnbuildableLogger implements PreparedLogger {
		private static final Logger LOGGER = Logging.getLogger(WarnUnbuildableLogger.class);
		private final String projectPath;

		@Override
		public void log() {
			LOGGER.warn("'main' component in project '" + projectPath + "' cannot build on this machine.");
		}
	}

	@RequiredArgsConstructor
	private static class OneTimeLogger implements PreparedLogger {
		private final PreparedLogger delegate;
		private boolean messageAlreadyLogged = false;

		@Override
		public void log() {
			if (!messageAlreadyLogged) {
				delegate.log();
				messageAlreadyLogged = true;
			}
		}
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
