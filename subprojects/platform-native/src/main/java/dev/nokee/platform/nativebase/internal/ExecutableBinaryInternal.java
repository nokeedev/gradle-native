package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ExecutableBinaryInternal extends BaseNativeBinary implements ExecutableBinary, Buildable {

	@Inject
	public ExecutableBinaryInternal(NamingScheme names, DomainObjectSet<GeneratedSourceSet> objectSourceSets, DefaultTargetMachine targetMachine, TaskProvider<LinkExecutableTask> linkTask, NativeDependencies dependencies) {
		super(names, objectSourceSets, targetMachine, dependencies);

		linkTask.configure(this::configureExecutableTask);
		linkTask.configure(task -> {
			task.getLibs().from(dependencies.getLinkLibraries());
			task.getLinkerArgs().addAll(getProviders().provider(() -> dependencies.getLinkFrameworks().getFiles().stream().flatMap(this::toFrameworkFlags).collect(Collectors.toList())));
		});
	}

	private Stream<String> toFrameworkFlags(File it) {
		return ImmutableList.of("-F", it.getParent(), "-framework", FilenameUtils.removeExtension(it.getName())).stream();
	}

	private void configureExecutableTask(LinkExecutableTask task) {
		task.setDescription("Links the executable.");
		task.source(getObjectFiles());

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		// Until we model the build type
		task.getDebuggable().set(false);

		task.getDestinationDirectory().convention(getLayout().getBuildDirectory().dir(getNames().getOutputDirectoryBase("exes")));
		task.getLinkedFile().convention(getExecutableLinkedFile());

		task.getToolChain().set(selectNativeToolChain(getTargetMachine()));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();
	}

	private Provider<RegularFile> getExecutableLinkedFile() {
		return getLayout().getBuildDirectory().file(getBaseName().map(it -> {
			OperatingSystemFamily osFamily = getTargetMachine().getOperatingSystemFamily();
			OperatingSystemOperations osOperations = OperatingSystemOperations.of(osFamily);
			return osOperations.getExecutableName(getNames().getOutputDirectoryBase("exes") + "/" + it);
		}));
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public TaskProvider<? extends LinkExecutable> getLinkTask() {
		return getTasks().named(getNames().getTaskName("link"), LinkExecutable.class);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(getLinkTask().get());
	}
}
