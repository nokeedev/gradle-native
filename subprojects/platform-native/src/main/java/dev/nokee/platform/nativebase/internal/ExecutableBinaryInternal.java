package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class ExecutableBinaryInternal extends BaseNativeBinary implements ExecutableBinary, Buildable {
	private final NamingScheme names;

	@Inject
	public ExecutableBinaryInternal(NamingScheme names, DomainObjectSet<GeneratedSourceSet> objectSourceSets, DefaultTargetMachine targetMachine) {
		super(objectSourceSets, targetMachine);
		this.names = names;
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public TaskProvider<? extends LinkExecutable> getLinkTask() {
		return getTasks().named(names.getTaskName("link"), LinkExecutable.class);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(getLinkTask().get());
	}
}
