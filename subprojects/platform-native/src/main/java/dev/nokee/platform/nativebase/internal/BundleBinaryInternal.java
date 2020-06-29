package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.LinkBundle;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class BundleBinaryInternal extends BaseNativeBinary implements BundleBinary, Buildable {
	private final TaskProvider<LinkBundleTask> linkTask;

	@Inject
	public BundleBinaryInternal(NamingScheme names, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets, TaskProvider<LinkBundleTask> linkTask, NativeIncomingDependencies dependencies) {
		super(names, objectSourceSets, targetMachine, dependencies);

		this.linkTask = linkTask;

		linkTask.configure(this::configureBundleTask);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	private void configureBundleTask(LinkBundleTask task) {
		task.setDescription("Links the bundle.");
		task.source(getObjectFiles());

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		// Until we model the build type
		task.getDebuggable().set(false);

		task.getDestinationDirectory().convention(getLayout().getBuildDirectory().dir(getNames().getOutputDirectoryBase("libs")));
		task.getLinkedFile().convention(getBundleLinkedFile());

		task.getLinkerArgs().addAll("-Xlinker", "-bundle"); // Required when not building swift

		task.getToolChain().set(selectNativeToolChain(getTargetMachine()));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();
	}

	private Provider<RegularFile> getBundleLinkedFile() {
		return getLayout().getBuildDirectory().file(getBaseName().map(it -> {
			OperatingSystemFamily osFamily = getTargetMachine().getOperatingSystemFamily();
			OperatingSystemOperations osOperations = OperatingSystemOperations.of(osFamily);
			return osOperations.getExecutableName(getNames().getOutputDirectoryBase("libs") + "/" + it);
		}));
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(getLinkTask().get());
	}

	@Override
	public TaskProvider<LinkBundle> getLinkTask() {
		return getTasks().named(linkTask.getName(), LinkBundle.class);
	}
}
