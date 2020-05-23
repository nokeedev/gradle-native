package dev.nokee.platform.jni.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.c.internal.CSourceSetTransform;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSetTransform;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetTransform;
import dev.nokee.language.objectivec.internal.UTTypeObjectiveCSource;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetTransform;
import dev.nokee.language.objectivecpp.internal.UTTypeObjectiveCppSource;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.NamingSchemeFactory;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.DefaultJvmJarBinary;
import dev.nokee.platform.jni.internal.JniLibraryDependenciesInternal;
import dev.nokee.platform.jni.internal.JniLibraryExtensionInternal;
import dev.nokee.platform.jni.internal.JniLibraryInternal;
import dev.nokee.platform.nativebase.TargetMachine;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.darwin.internal.plugins.DarwinFrameworkResolutionSupportPlugin;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
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
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

public abstract class JniLibraryPlugin implements Plugin<Project> {
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
		TaskContainer tasks = project.getTasks();
		project.getPluginManager().apply("base");
		project.getPluginManager().apply("lifecycle-base");
		project.getPluginManager().apply(StandardToolChainsPlugin.class);

		DefaultTargetMachineFactory targetMachineFactory = registerTargetMachineFactoryAsExtension(project.getExtensions());
		JniLibraryExtensionInternal extension = registerExtension(project, targetMachineFactory);

		// TODO: On `java` apply, just apply the `java-library` (but don't allow other users to apply it
		project.getPluginManager().withPlugin("java", appliedPlugin -> configureJavaJniRuntime(project, extension));
		project.getPluginManager().withPlugin("java", appliedPlugin -> registerJniHeaderSourceSet(project, extension));
		project.getPluginManager().withPlugin("java-library", appliedPlugin -> { throw new GradleException("Use java plugin instead"); });
		project.getPlugins().withType(NativePlatformCapabilitiesMarkerPlugin.class, appliedPlugin -> {
			project.getPluginManager().apply(DarwinFrameworkResolutionSupportPlugin.class);
		});

		// Names
		NamingSchemeFactory namingSchemeFactory = new NamingSchemeFactory(project.getName());
		NamingScheme mainComponentNames = namingSchemeFactory.forMainComponent();

		project.afterEvaluate(proj -> {
			extension.getTargetMachines().disallowChanges();
			extension.getTargetMachines().finalizeValue();
			Set<TargetMachine> targetMachines = extension.getTargetMachines().get();
			assertNonEmpty(extension.getTargetMachines().get(), "target machine", "library");
			assertTargetMachinesAreKnown(targetMachines);

			Optional<DefaultJvmJarBinary> jvmJarBinary = findJvmBinary(proj);
			targetMachines.stream().forEach(targetMachine -> {
				DefaultTargetMachine targetMachineInternal = (DefaultTargetMachine)targetMachine;

				NamingScheme namingScheme = mainComponentNames;
				namingScheme = namingScheme.withVariantDimension(targetMachineInternal.getOperatingSystemFamily(), targetMachinesToOperatingSystems(targetMachines));
				namingScheme = namingScheme.withVariantDimension(targetMachineInternal.getArchitecture(), targetMachinesToArchitectures(targetMachines));

				final NamingScheme names = namingScheme;

				// Find toolchain capable of building C++
				final NamedDomainObjectProvider<JniLibraryInternal> library = extension.registerVariant(names, targetMachineInternal, it -> {
					// Build all language source set
					List<SourceSet<UTTypeObjectCode>> objectSourceSets = new ArrayList<>();
					if (proj.getPluginManager().hasPlugin("dev.nokee.cpp-language")) {
						SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(CppSourceSet.class).srcDir("src/main/cpp").transform(getObjects().newInstance(CppSourceSetTransform.class, names, targetMachineInternal, toolChainSelector));
						objectSourceSets.add(objectSourceSet);
					}
					if (proj.getPluginManager().hasPlugin("dev.nokee.c-language")) {
						SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(CSourceSet.class).srcDir("src/main/c").transform(getObjects().newInstance(CSourceSetTransform.class, names, targetMachineInternal, toolChainSelector));
						objectSourceSets.add(objectSourceSet);
					}
					if (proj.getPluginManager().hasPlugin("dev.nokee.objective-cpp-language")) {
						SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCppSource()).srcDir("src/main/objcpp").transform(getObjects().newInstance(ObjectiveCppSourceSetTransform.class, names, targetMachineInternal, toolChainSelector));
						objectSourceSets.add(objectSourceSet);
					}
					if (proj.getPluginManager().hasPlugin("dev.nokee.objective-c-language")) {
						SourceSet<UTTypeObjectCode> objectSourceSet = getObjects().newInstance(DefaultSourceSet.class, new UTTypeObjectiveCSource()).srcDir("src/main/objc").transform(getObjects().newInstance(ObjectiveCSourceSetTransform.class, names, targetMachineInternal, toolChainSelector));
						objectSourceSets.add(objectSourceSet);
					}

					TaskProvider<LinkSharedLibraryTask> linkTask = tasks.register(names.getTaskName("link"), LinkSharedLibraryTask.class, task -> {
						objectSourceSets.stream().map(SourceSet::getAsFileTree).forEach(task::source);

						NativePlatformFactory nativePlatformFactory = new NativePlatformFactory();
						NativePlatformInternal nativePlatform = nativePlatformFactory.create(targetMachine);
						task.getTargetPlatform().set(nativePlatform);
						task.getTargetPlatform().finalizeValueOnRead();
						task.getTargetPlatform().disallowChanges();

						// Until we model the build type
						task.getDebuggable().set(false);

						// Install name set inside SharedLibraryBinaryInternal

						task.getDestinationDirectory().convention(getLayout().getBuildDirectory().dir(names.getOutputDirectoryBase("libs")));
						task.getLinkedFile().convention(getLayout().getBuildDirectory().file(nativePlatform.getOperatingSystem().getInternalOs().getSharedLibraryName(names.getOutputDirectoryBase("libs") + "/" + project.getName())));

						task.getToolChain().set(getProviders().provider(() -> toolChainSelector.select(targetMachine)));
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

				getTasks().register(names.getTaskName("objects"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles main objects.");
					task.dependsOn(library.map(it -> it.getSharedLibrary().getCompileTasks()));
				});

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
						getTasks().named("jar", Jar.class, configureJarTaskUsing(library));

						// NOTE: We don't need to attach the JNI JAR to runtimeElements as the `java` plugin take cares of this.
					} else {
						TaskProvider<Jar> jarTask = getTasks().register("jar", Jar.class, configureJarTaskUsing(library));

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
					if (toolChainSelector.canBuild(targetMachine)) {
						// TODO: We could maybe set the shared library directory as secondary variant.
						//  However, the shared library would requires the resource path to be taken into consideration...
						getConfigurations().named("runtimeElements", it -> it.getOutgoing().artifact(jarTask.flatMap(Jar::getArchiveFile)));
					}
				}


				// Attach JNI Jar to assemble task
				if (DefaultTargetMachine.isTargetingHost().test(targetMachine)) {
					// Attach JNI Jar to assemble
					project.getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, it -> {
						it.dependsOn(library.map(l -> l.getJar().getJarTask()));
					});
				}
			});
		});

		// Ensure the variants are resolved so all tasks are registered.
		tasks.named("tasks", task -> {
			task.dependsOn((Callable) () -> {
				// TODO: Account for no variant, is that even possible?
				extension.getVariantCollection().iterator().next();
				return emptyList();
			});
		});
		// Ensure the variants are resolved so all configurations and dependencies are registered.
		tasks.named("dependencies", task -> {
			task.dependsOn((Callable) () -> {
				// TODO: Account for no variant, is that even possible?
				extension.getVariantCollection().iterator().next();
				return emptyList();
			});
		});
		tasks.named("outgoingVariants", task -> {
			task.dependsOn((Callable) () -> {
				// TODO: Account for no variant, is that even possible?
				extension.getVariantCollection().iterator().next();
				return emptyList();
			});
		});
		// Differ this rules until after the project is evaluated to avoid interfering with other plugins
		project.afterEvaluate(proj -> {
			// The previous trick doesn't work for dependencyInsight task and vice-versa.
			project.getConfigurations().addRule("Java Native Interface (JNI) variants are resolved only when needed.", it -> {
				// TODO: Account for no variant, is that even possible?
				extension.getVariantCollection().iterator().next();
			});
		});

		// Warn if component is cannot build on this machine
		tasks.named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, task -> {
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
			task.from(library.map(JniLibraryInternal::getNativeRuntimeFiles), spec -> {
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

	private static Set<DefaultOperatingSystemFamily> targetMachinesToOperatingSystems(Collection<TargetMachine> targetMachines) {
		return targetMachines.stream().map(it -> ((DefaultTargetMachine)it).getOperatingSystemFamily()).collect(Collectors.toSet());
	}

	private static Set<DefaultMachineArchitecture> targetMachinesToArchitectures(Collection<TargetMachine> targetMachines) {
		return targetMachines.stream().map(it -> ((DefaultTargetMachine)it).getArchitecture()).collect(Collectors.toSet());
	}

	private static DefaultTargetMachineFactory registerTargetMachineFactoryAsExtension(ExtensionContainer extensions) {
		DefaultTargetMachineFactory targetMachineFactory = new DefaultTargetMachineFactory();
		extensions.add(TargetMachineFactory.class, "machines", targetMachineFactory);
		return targetMachineFactory;
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

	private JniLibraryExtensionInternal registerExtension(Project project, DefaultTargetMachineFactory targetMachineFactory) {
		JniLibraryDependenciesInternal dependencies = project.getObjects().newInstance(JniLibraryDependenciesInternal.class);
		Configuration jvmApiElements = Optional.ofNullable(project.getConfigurations().findByName("apiElements")).orElseGet(() -> {
			return project.getConfigurations().create("apiElements", configuration -> {
				configuration.setCanBeResolved(false);
				configuration.setCanBeConsumed(true);
				configuration.attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API));
					attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR));
				});
			});
		});
		jvmApiElements.extendsFrom(dependencies.getApiDependencies());


		Configuration jvmRuntimeElements = Optional.ofNullable(project.getConfigurations().findByName("runtimeElements")).orElseGet(() -> {
			return project.getConfigurations().create("runtimeElements", configuration -> {
				configuration.setCanBeResolved(false);
				configuration.setCanBeConsumed(true);
				configuration.attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
					attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, LibraryElements.JAR));
				});
			});
		});
		jvmRuntimeElements.extendsFrom(dependencies.getApiDependencies());

		JniLibraryExtensionInternal library = project.getObjects().newInstance(JniLibraryExtensionInternal.class, dependencies, GroupId.of(project::getGroup));
		library.getTargetMachines().convention(singletonList(targetMachineFactory.host()));
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
			Provider<List<FileCollection>> files = getProviders().provider(() -> library.getVariantCollection().stream().map(it -> it.getNativeRuntimeFiles()).collect(Collectors.toList()));
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
}
