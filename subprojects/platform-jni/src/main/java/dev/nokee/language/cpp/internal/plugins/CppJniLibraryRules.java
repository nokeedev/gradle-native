package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.jvm.internal.JvmResourceSetInternal;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.jni.internal.JniLibraryInternal;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Usage;
import org.gradle.api.internal.project.ProjectIdentifier;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.cpp.CppBinary;
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.model.Finalize;
import org.gradle.model.Model;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.StaticLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.LinkSharedLibrary;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.internal.BinarySpecInternal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.MetaClassHelper.capitalize;

public class CppJniLibraryRules extends RuleSource {
    @Model
    public JniLibraryInternal library(ExtensionContainer extensions) {
        return (JniLibraryInternal)extensions.getByType(JniLibrary.class);
    }

    @Mutate
    public void createNativeLibrary(ComponentSpecContainer components, ProjectIdentifier projectIdentifier) {
        components.create("main", NativeLibrarySpec.class, library -> {
            library.setBaseName(projectIdentifier.getName());

            // Disable the StaticLibrary as we don't need it for JNI libraries
            library.getBinaries().withType(StaticLibraryBinarySpec.class, binary -> {
                ((BinarySpecInternal)binary).setBuildable(false);
            });
        });
    }

    // Remove software model tasks from displaying when invoking `./gradlew tasks`
    // These tasks are an implementation details to what we are trying to achieve here.
    // The users should call the tasks that Nokee Labs vet as public APIs.
    @Finalize
    public void hideSoftwareModelTasks(TaskContainer tasks, ComponentSpecContainer components) {
        Action<? super Task> hideTask = task -> {
            task.setGroup(null);
        };

        NativeLibrarySpec library = components.withType(NativeLibrarySpec.class).get("main");

        library.getBinaries().values().forEach(binary -> {
            // Hide binary registered tasks
            binary.getTasks().all(hideTask);

            // Hide binary lifecycle task
            hideTask.execute(binary.getTasks().getBuild());
        });

        // Hide dependents lifecycle tasks
        tasks.named("assembleDependents" + capitalize(library.getName()), hideTask);
        tasks.named("buildDependents" + capitalize(library.getName()), hideTask);

        // Hide help tasks
        tasks.named("components", hideTask);
        tasks.named("dependentComponents", hideTask);
    }

    @Mutate
    public void configureJniLibrary(JniLibraryInternal library, ComponentSpecContainer components, ProjectIdentifier projectIdentifier) {
        Collection<SharedLibraryBinarySpec> binaries = components.withType(NativeLibrarySpec.class).get("main").getBinaries().withType(SharedLibraryBinarySpec.class).values();
        if (binaries.size() > 1) {
            throw new IllegalStateException("More binaries than predicted");
        }
        library.getBinaries().addAll(binaries.stream().filter(SharedLibraryBinarySpec::isBuildable).collect(Collectors.toList()));


        Project project = (Project) projectIdentifier;
        ConfigurationContainer configurations = project.getConfigurations();

        /*
         * Define some configurations to present the outputs of this build
         * to other Gradle projects.
         */
        final Usage cppApiUsage = project.getObjects().named(Usage.class, Usage.C_PLUS_PLUS_API);
        final Usage linkUsage = project.getObjects().named(Usage.class, Usage.NATIVE_LINK);

        Configuration implementation = configurations.getByName("nativeImplementation");

        // incoming compile time headers - this represents the headers we consume
        Configuration cppCompile = project.getConfigurations().create("cppCompile", it -> {
            it.setCanBeConsumed(false);
            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, cppApiUsage);
        });

        // incoming linktime libraries (i.e. static libraries) - this represents the libraries we consume
        Configuration cppLinkDebug = project.getConfigurations().create("cppLinkDebug", it -> {
            it.setCanBeConsumed(false);
            it.extendsFrom(implementation);
            it.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, linkUsage);
            it.getAttributes().attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, true);
            it.getAttributes().attribute(CppBinary.OPTIMIZED_ATTRIBUTE, false);
        });

        library.getSources().withType(JvmResourceSetInternal.class, resourceSet -> {
            binaries.forEach(binary -> {
                resourceSet.getSource().from(binary.getSharedLibraryFile()).builtBy(binary);
            });
        });

        library.getBinaries().all(binary -> {
            binary.getTasks().withType(CppCompile.class, task -> {
                // configure includes using the native incoming compile configuration
                task.setDebuggable(true);
                task.setOptimized(false);
                task.includes(cppCompile);

                library.getSources().withType(HeaderExportingSourceSetInternal.class, sourceSet -> task.getIncludes().from(sourceSet.getSource()));

                task.getIncludes().from(project.provider(() -> {
                    List<File> result = new ArrayList<>();
                    result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include"));

                    if (OperatingSystem.current().isMacOsX()) {
                        result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/darwin"));
                    } else if (OperatingSystem.current().isLinux()) {
                        result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/linux"));
                    } else if (OperatingSystem.current().isWindows()) {
                        result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/win32"));
                    }
                    return result;
                }));
            });

            binary.getTasks().withType(LinkSharedLibrary.class, task -> {
                task.getLibs().from(cppLinkDebug);
            });
        });
    }

    @Finalize
    public void forceResolve(TaskContainer tasks, JniLibraryInternal library) {

    }
}
