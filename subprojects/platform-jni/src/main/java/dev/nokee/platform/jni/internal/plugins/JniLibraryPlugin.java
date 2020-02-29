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
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.cpp.CppBinary;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;

public class JniLibraryPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("lifecycle-base");

        JniLibraryInternal library = registerExtension(project);

        project.getPluginManager().withPlugin("java", appliedPlugin -> configureJavaJniRuntime(project, library));
        project.getPluginManager().withPlugin("java", appliedPlugin -> registerJniHeaderSourceSet(project, library));
        project.getPluginManager().withPlugin("java-library", appliedPlugin -> { throw new GradleException("Use java plugin instead"); });

        exportHeaders(project, library);
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

    private void registerJniHeaderSourceSet(Project project, JniLibraryInternal library) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet main = sourceSets.getByName("main");

        Provider<Directory> jniHeaderDirectory = project.getLayout().getBuildDirectory().dir("jniHeaders");
        TaskProvider<JavaCompile> compileTask = project.getTasks().named(main.getCompileJavaTaskName(), JavaCompile.class, task -> {
            task.getOutputs().dir(jniHeaderDirectory);
            task.getOptions().getCompilerArgumentProviders().add(() -> Arrays.asList("-h", jniHeaderDirectory.get().getAsFile().getAbsolutePath()));
        });
        ConfigurableFileCollection jniHeaders = project.files(jniHeaderDirectory, config -> {
            config.builtBy(compileTask);
        });
        HeaderExportingSourceSetInternal jniHeaderSourceSet = project.getObjects().newInstance(HeaderExportingSourceSetInternal.class);
        jniHeaderSourceSet.getSource().from(jniHeaders);
        library.getSources().add(jniHeaderSourceSet);
    }

    private void registerJvmResourceSet(Project project, JniLibraryInternal library) {
        Usage runtimeUsage = project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME);
        Configuration implementation = library.getNativeImplementationDependencies();

        // TODO: Consume according to the "target" that people will need
        // incoming runtime libraries (i.e. shared libraries) - this represents the libraries we consume
        Configuration nativeRuntimeRelease = project.getConfigurations().create("nativeRuntimeRelease", it -> {
            it.setCanBeConsumed(false);
            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, runtimeUsage);
            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, true);
        });

        JvmResourceSetInternal resourceSet = project.getObjects().newInstance(JvmResourceSetInternal.class);
        resourceSet.getSource().from(nativeRuntimeRelease);
        library.getSources().add(resourceSet);
    }

    private void exportHeaders(Project project, JniLibraryInternal library) {
        Usage cppApiUsage = project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API);

        // outgoing public headers - this represents the headers we expose (including transitive headers)
        Configuration headers = project.getConfigurations().create("headers", it -> {
            it.setCanBeResolved(false);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, cppApiUsage);
            it.getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, DIRECTORY_TYPE);

            library.getSources().withType(HeaderExportingSourceSetInternal.class, sourceSet -> {
                sourceSet.getSource().getFiles().forEach(includeRoot -> {
                    it.getOutgoing().artifact(includeRoot);
                });
            });
        });

        // Export JVM headers
        // TODO: The headers should not be published
        // TODO: Create outgoing artifact using attributes, it should event be a runtime dependency (we don't want the headers to be published.
        getJvmIncludeRoots().forEach(includeRoot -> headers.getOutgoing().artifact(includeRoot));
    }

    public static List<File> getJvmIncludeRoots() {
        List<File> result = new ArrayList<>();
        result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include"));

        // TODO: Create outgoing artifact using attributes, it should event be a runtime dependency (we don't want the headers to be published.
        if (OperatingSystem.current().isMacOsX()) {
            result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/darwin"));
        } else if (OperatingSystem.current().isLinux()) {
            result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/linux"));
        }
        return result;
    }

    private void configureJavaJniRuntime(Project project, JniLibraryInternal library) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet main = sourceSets.getByName("main");

        // Wire JVM to JniLibrary
        project.getConfigurations().getByName("implementation").extendsFrom(library.getJvmImplementationDependencies());

        /*
         * Define some configurations to present the outputs of this build
         * to other Gradle projects.
         */
        final Usage cppApiUsage = project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API);
        final Usage linkUsage = project.getObjects().named(Usage.class, Usage.NATIVE_LINK);
        final Usage runtimeUsage = project.getObjects().named(Usage.class, Usage.NATIVE_RUNTIME);

        // dependencies of the library
//        Configuration implementation = project.getConfigurations().create("implementation", it -> {
//            it.setCanBeConsumed(false);
//            it.setCanBeResolved(false);
//        });

//        // incoming compile time headers - this represents the headers we consume
//        project.getConfigurations().create("cppCompile", it -> {
//            it.setCanBeConsumed(false);
//            it.extendsFrom(implementation);
//            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, cppApiUsage);
//        });

//        // incoming linktime libraries (i.e. static libraries) - this represents the libraries we consume
//        project.getConfigurations().create("cppLinkDebug", it -> {
//            it.setCanBeConsumed(false);
//            it.extendsFrom(implementation);
//            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, linkUsage);
//            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
//            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
//        });
//        project.getConfigurations().create("cppLinkRelease", it -> {
//            it.setCanBeConsumed(false);
//            it.extendsFrom(implementation);
//            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, linkUsage);
//            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
//            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, true);
//        });

//        // incoming runtime libraries (i.e. shared libraries) - this represents the libraries we consume
//        project.getConfigurations().create("cppRuntimeDebug", it -> {
//            it.setCanBeConsumed(false);
//            it.extendsFrom(implementation);
//            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, runtimeUsage);
//            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
//            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
//        });
//        project.getConfigurations().create("cppRuntimeRelease", it -> {
//            it.setCanBeConsumed(false);
//            it.extendsFrom(implementation);
//            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, runtimeUsage);
//            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
//            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, true);
//        });



        // outgoing linktime libraries (i.e. static libraries) - this represents the libraries we expose (including transitive headers)
        Configuration linkDebug = project.getConfigurations().create("linkDebug", it -> {
            it.setCanBeResolved(false);
//            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, linkUsage);
            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
        });
        Configuration linkRelease = project.getConfigurations().create("linkRelease", it -> {
            it.setCanBeResolved(false);
//            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, linkUsage);
            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, true);
        });

        // outgoing runtime libraries (i.e. shared libraries) - this represents the libraries we expose (including transitive headers)
        Configuration runtimeDebug = project.getConfigurations().create("runtimeDebug", it -> {
            it.setCanBeResolved(false);
//            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, runtimeUsage);
            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
        });
        Configuration runtimeRelease = project.getConfigurations().create("runtimeRelease", it -> {
            it.setCanBeResolved(false);
//            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, runtimeUsage);
            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, true);
        });




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
