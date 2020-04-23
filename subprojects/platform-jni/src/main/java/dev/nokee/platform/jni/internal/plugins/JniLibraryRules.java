package dev.nokee.platform.jni.internal.plugins;

import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.JniLibraryExtensionInternal;
import dev.nokee.platform.nativebase.internal.NativePlatformFactory;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import dev.nokee.platform.nativebase.internal.ToolChainSelectorInternal;
import org.gradle.api.Action;
import org.gradle.api.Project;
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
import org.gradle.platform.base.PlatformContainer;
import org.gradle.platform.base.internal.BinarySpecInternal;

import java.util.Collection;

import static dev.nokee.platform.nativebase.internal.NativePlatformFactory.platformNameFor;
import static org.codehaus.groovy.runtime.MetaClassHelper.capitalize;

public class JniLibraryRules extends RuleSource {
	@Model
	public JniLibraryExtensionInternal library(ProjectIdentifier projectIdentifier) {
		// Going through the ProjectIdentifier route to break the Software Model cycle with ExtensionContainer and other model elements bridged from the Software Model.
		Project project = (Project)projectIdentifier;
		ExtensionContainer extensions = project.getExtensions();
		return (JniLibraryExtensionInternal)extensions.getByType(JniLibraryExtension.class);
	}

	@Mutate
	public void createNativeLibrary(ComponentSpecContainer components, ProjectIdentifier projectIdentifier) {
		components.create("main", NativeLibrarySpec.class, library -> {
			library.setBaseName(projectIdentifier.getName());

			// Disable the StaticLibrary as we don't need it for JNI libraries
			library.getBinaries().withType(StaticLibraryBinarySpec.class, binary -> {
				((BinarySpecInternal) binary).setBuildable(false);
			});
		});
	}

	@Mutate
	public void configureTargetMachines(PlatformContainer platforms, JniLibraryExtensionInternal extension) {
		NativePlatformFactory nativePlatformFactory = new NativePlatformFactory();
		extension.getTargetMachines().get().stream().forEach(targetMachine -> {
			platforms.add(nativePlatformFactory.create(targetMachine));
		});
	}

	@Mutate
	public void configureLibraryTargetMachines(@Path("components.main") NativeLibrarySpec library, JniLibraryExtensionInternal extension, ProjectIdentifier projectIdentifier) {
		ToolChainSelectorInternal toolChainSelector = ((Project)projectIdentifier).getObjects().newInstance(ToolChainSelectorInternal.class);
		extension.getTargetMachines().get().stream().forEach(targetMachine -> {
			library.targetPlatform(platformNameFor(targetMachine));
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
	public void configureJniLibrary(TaskContainer tasks, @Path("components.main") NativeLibrarySpec nativeLibrary, JniLibraryExtensionInternal extension) {
		Collection<SharedLibraryBinarySpec> binaries = nativeLibrary.getBinaries().withType(SharedLibraryBinarySpec.class).values();
		// TODO: Attach binary to the right variant
		extension.getVariantCollection().forEach(library -> {
			SharedLibraryBinarySpec binary = binaries.stream().filter(it -> platformNameFor(library.getTargetMachine()).equals(it.getTargetPlatform().getName())).findFirst().orElseThrow(() -> new RuntimeException("No binary available"));
			SharedLibraryBinaryInternal sharedLibrary = library.getSharedLibrary();
			sharedLibrary.configureSoftwareModelBinary(binary);
			sharedLibrary.getLinkedFile().set(((LinkSharedLibrary)binary.getTasks().getLink()).getLinkedFile());
			sharedLibrary.getLinkedFile().disallowChanges();
		});
	}
}
