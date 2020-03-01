package dev.nokee.platform.jni.internal.plugins;

import dev.nokee.language.jvm.internal.JvmResourceSetInternal;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.internal.JniLibraryInternal;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.cpp.CppBinary;
import org.gradle.util.GradleVersion;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class JniLibraryPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("lifecycle-base");

        JniLibraryInternal library = registerExtension(project);

        project.getPluginManager().withPlugin("java", appliedPlugin -> configureJavaJniRuntime(project, library));
        project.getPluginManager().withPlugin("java", appliedPlugin -> registerJniHeaderSourceSet(project, library));
        project.getPluginManager().withPlugin("java-library", appliedPlugin -> { throw new GradleException("Use java plugin instead"); });

        registerJvmResourceSet(project, library);
    }

    private JniLibraryInternal registerExtension(Project project) {
        Configuration nativeImplementation = project.getConfigurations().create("nativeImplementation", configuration -> {
            configuration.setCanBeResolved(false);
            configuration.setCanBeConsumed(false);
        });

        Configuration jvmImplementation = project.getConfigurations().create("jvmImplementation", configuration -> {
            configuration.setCanBeResolved(false);
            configuration.setCanBeConsumed(false);
        });

        JniLibraryInternal library = project.getObjects().newInstance(JniLibraryInternal.class, nativeImplementation, jvmImplementation);
        project.getExtensions().add(JniLibrary.class, "library", library);
        return library;
    }

    private static boolean isGradleVersionGreaterOrEqualsTo6Dot3() {
        return GradleVersion.current().compareTo(GradleVersion.version("6.3")) >= 0;
    }

    private void registerJniHeaderSourceSet(Project project, JniLibraryInternal library) {
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

    private void registerJvmResourceSet(Project project, JniLibraryInternal library) {
        Usage runtimeUsage = project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME);
        Configuration implementation = library.getNativeImplementationDependencies();

        // TODO: Consume according to the "target" that people will need
        // incoming runtime libraries (i.e. shared libraries) - this represents the libraries we consume
        Configuration nativeRuntimeRelease = project.getConfigurations().create("nativeRuntimeDebug", it -> {
            it.setCanBeConsumed(false);
            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, runtimeUsage);
            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
        });

        JvmResourceSetInternal resourceSet = project.getObjects().newInstance(JvmResourceSetInternal.class);
        resourceSet.getSource().from(nativeRuntimeRelease);
        library.getSources().add(resourceSet);
    }

    private void configureJavaJniRuntime(Project project, JniLibraryInternal library) {
        // Wire JVM to JniLibrary
        project.getConfigurations().getByName("implementation").extendsFrom(library.getJvmImplementationDependencies());

        project.getTasks().named("test", Test.class, task -> {
            task.dependsOn(library.getSources().withType(JvmResourceSetInternal.class).stream().map(JvmResourceSetInternal::getSource).collect(Collectors.toList()));
            // TODO: Notify when no native library exists
            String path = library.getSources().withType(JvmResourceSetInternal.class).stream().map(JvmResourceSetInternal::getSource).map(FileCollection::getFiles).flatMap(Collection::stream).map(File::getParent).collect(joining(File.pathSeparator));
            task.systemProperty("java.library.path", path);
        });

        project.getTasks().named("jar", Jar.class, task -> {
            task.from(library.getSources().withType(JvmResourceSetInternal.class).stream().map(JvmResourceSetInternal::getSource).collect(Collectors.toList()));
        });
    }
}
