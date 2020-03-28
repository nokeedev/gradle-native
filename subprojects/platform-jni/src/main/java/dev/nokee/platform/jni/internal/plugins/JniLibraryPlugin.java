package dev.nokee.platform.jni.internal.plugins;

import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.*;
import dev.nokee.platform.nativebase.TargetMachine;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.platform.nativebase.internal.plugins.FakeMavenRepositoryPlugin;
import dev.nokee.platform.nativebase.internal.plugins.NativePlatformCapabilitiesMarkerPlugin;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.nativeplatform.internal.toolchains.ToolChainSelector;
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

public abstract class JniLibraryPlugin implements Plugin<Project> {
	private final ToolChainSelectorInternal toolChainSelector = getObjects().newInstance(ToolChainSelectorInternal.class);

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
			project.getPluginManager().apply(JniLibraryRules.class);
			project.getPluginManager().apply(FakeMavenRepositoryPlugin.class);
		});

		// Include native runtime files inside JNI jar
		extension.getVariants().configureEach(library -> {
			library.getJar().getJarTask().configure(task -> task.from(library.getNativeRuntimeFiles(), spec -> spec.into(library.getResourcePath().get())));
		});

		// Attach JNI Jar to runtimeElements
		extension.getVariants().configureEach(library -> {
			// TODO: Maybe go through the source set instead
			// TODO: Expose Jar on runtimeElements but the directory where the shared library is located
			if (extension.getTargetMachines().get().size() > 1 || !project.getPluginManager().hasPlugin("java")) {
				project.getConfigurations().getByName("runtimeElements").getOutgoing().artifact(library.getJar().getArchiveFile());
			}
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

			targetMachines.stream().filter(toolChainSelector::canBuild).forEach(targetMachine -> {

				NamingScheme names = mainComponentNames;
				names = names.withVariantDimension((DefaultOperatingSystemFamily)targetMachine.getOperatingSystemFamily(), targetMachinesToOperatingSystems(targetMachines));
				names = names.withVariantDimension((DefaultMachineArchitecture)targetMachine.getArchitecture(), targetMachinesToArchitectures(targetMachines));

				// Find toolchain capable of building C++
				JniLibraryInternal library = extension.newVariant(names, targetMachine);
				if (proj.getPluginManager().hasPlugin("dev.nokee.cpp-language") || proj.getPluginManager().hasPlugin("dev.nokee.c-language") || proj.getPluginManager().hasPlugin("dev.nokee.objective-c-language") || proj.getPluginManager().hasPlugin("dev.nokee.objective-cpp-language")) {
					library.registerSharedLibraryBinary();
				}

				if (proj.getPluginManager().hasPlugin("java") && targetMachines.size() == 1) {
					TaskProvider<Jar> jvmJarTask = project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class);
					library.registerJniJarBinary(jvmJarTask);
					library.getAssembleTask().configure(task -> task.dependsOn(jvmJarTask));
				} else {
					library.registerJniJarBinary();
					if (proj.getPluginManager().hasPlugin("java")) {
						library.getAssembleTask().configure(task -> task.dependsOn(project.getTasks().named(JavaPlugin.JAR_TASK_NAME, Jar.class)));
					} else {
						// FIXME: There is a gap here, if the project doesn't have any JVM plugin applied but specify multiple target machine what is expected?
						//   Only JNI Jar? or an empty JVM Jar and JNI Jar?... Hmmm....
					}
				}


				// Attach JNI Jar to assemble task
				if (DefaultTargetMachine.isTargetingHost().test(targetMachine)) {
					// Attach JNI Jar to assemble
					project.getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, it -> it.dependsOn(library.getJar().getJarTask()));
				}

				extension.getVariantCollection().add(library);
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
		SourceSet main = sourceSets.getByName("main");

		TaskProvider<JavaCompile> compileTask = project.getTasks().named(main.getCompileJavaTaskName(), JavaCompile.class, task -> {
			task.getOptions().getHeaderOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("generated/jni-headers"));

			// The nested output is not marked automatically as an output of the task regarding task dependencies.
			// So we mark it manually here.
			// See https://github.com/gradle/gradle/issues/6619.
			task.getOutputs().dir(task.getOptions().getHeaderOutputDirectory());

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
			List<FileCollection> files = library.getVariantCollection().stream().map(it -> it.getNativeRuntimeFiles()).collect(Collectors.toList());
			task.dependsOn(files);

			// TODO: notify when no native library exists
			String path = files.stream().flatMap(it -> it.getFiles().stream()).map(it -> it.getParentFile().getAbsolutePath()).collect(joining(File.pathSeparator));
			task.systemProperty("java.library.path", path);
		});
	}
}
