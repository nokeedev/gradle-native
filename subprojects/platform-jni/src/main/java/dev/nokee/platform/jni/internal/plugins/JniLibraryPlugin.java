package dev.nokee.platform.jni.internal.plugins;

import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.JniLibraryExtensionInternal;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class JniLibraryPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");

		JniLibraryExtensionInternal extension = registerExtension(project);

		project.getPluginManager().withPlugin("java", appliedPlugin -> configureJavaJniRuntime(project, extension));
		project.getPluginManager().withPlugin("java", appliedPlugin -> registerJniHeaderSourceSet(project, extension));
		project.getPluginManager().withPlugin("java-library", appliedPlugin -> { throw new GradleException("Use java plugin instead"); });

		// Include native runtime files inside JNI jar
		extension.getVariants().configureEach(library -> {
			library.getJar().getJarTask().configure(it -> it.from(library.getNativeRuntimeFiles()));
		});

		// Attach JNI Jar to assemble
		extension.getVariants().configureEach(library -> {
			project.getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, it -> it.dependsOn(library.getJar().getJarTask()));
		});

		// Attach JNI Jar to runtimeElements
		project.getPluginManager().withPlugin("java", appliedPlugin -> {
			extension.getVariants().configureEach(library -> {
				// TODO: Maybe go through the source set instead
				// TODO: Expose Jar on runtimeElements but the directory where the shared library is located
				project.getConfigurations().getByName("runtimeElements").getOutgoing().artifact(library.getJar().getArchiveFile());
			});
		});

		project.afterEvaluate(proj -> {
			if (proj.getPluginManager().hasPlugin("dev.nokee.cpp-language")) {
				// Find toolchain capable of building C++
				extension.registerVariant();
			}
		});
	}

	private JniLibraryExtensionInternal registerExtension(Project project) {
		Configuration nativeImplementation = project.getConfigurations().create("nativeImplementation", configuration -> {
			configuration.setCanBeResolved(false);
			configuration.setCanBeConsumed(false);
		});

		Configuration jvmImplementation = project.getConfigurations().create("jvmImplementation", configuration -> {
			configuration.setCanBeResolved(false);
			configuration.setCanBeConsumed(false);
		});

		JniLibraryExtensionInternal library = project.getObjects().newInstance(JniLibraryExtensionInternal.class, project.getConfigurations(), nativeImplementation, jvmImplementation);
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
			List<FileCollection> files = library.getVariants().stream().map(it -> it.getNativeRuntimeFiles()).collect(Collectors.toList());
			task.dependsOn(files);

			// TODO: notify when no native library exists
			String path = files.stream().flatMap(it -> it.getFiles().stream()).map(it -> it.getParentFile().getAbsolutePath()).collect(joining(File.pathSeparator));
			task.systemProperty("java.library.path", path);
		});
	}
}
