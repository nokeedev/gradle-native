package dev.nokee.language.cpp.internal.plugins;

import dev.nokee.language.jvm.internal.JvmResourceSetInternal;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.JniLibraryExtensionInternal;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectIdentifier;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.model.*;
import org.gradle.nativeplatform.NativeLibrarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.StaticLibraryBinarySpec;
import org.gradle.nativeplatform.tasks.LinkSharedLibrary;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.internal.BinarySpecInternal;

import java.util.Collection;

import static org.codehaus.groovy.runtime.MetaClassHelper.capitalize;

public class CppJniLibraryRules extends RuleSource {
    @Model
    public JniLibraryExtensionInternal library(ExtensionContainer extensions) {
        return (JniLibraryExtensionInternal)extensions.getByType(JniLibraryExtension.class);
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
    public void hideSoftwareModelTasks(TaskContainer tasks, @Path("components.main") NativeLibrarySpec library) {
        Action<? super Task> hideTask = task -> {
            task.setGroup(null);
        };

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
    public void configureJniLibrary(TaskContainer tasks, @Path("components.main") NativeLibrarySpec nativeLibrary, ProjectIdentifier projectIdentifier, JniLibraryExtensionInternal library) {
        Collection<SharedLibraryBinarySpec> binaries = nativeLibrary.getBinaries().withType(SharedLibraryBinarySpec.class).values();
        if (binaries.size() > library.getVariants().size()) {
            throw new IllegalStateException("More binaries than predicted");
        }
        SharedLibraryBinarySpec binary = binaries.iterator().next();
		SharedLibraryBinaryInternal sharedLibrary = library.getVariants().iterator().next().getSharedLibrary();
        sharedLibrary.configureSoftwareModelBinary(binary);
		sharedLibrary.getLinkedFile().set(((LinkSharedLibrary)binary.getTasks().getLink()).getLinkedFile());
		sharedLibrary.getLinkedFile().disallowChanges();

		// TODO remove
        library.getSources().withType(JvmResourceSetInternal.class, resourceSet -> {
			resourceSet.getSource().from(binary.getSharedLibraryFile()).builtBy(binary);
        });
    }
}
