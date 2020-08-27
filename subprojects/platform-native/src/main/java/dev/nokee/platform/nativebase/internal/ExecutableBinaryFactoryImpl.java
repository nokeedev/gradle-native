package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.tasks.ComponentTasksInternal;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

public class ExecutableBinaryFactoryImpl implements ExecutableBinaryFactory {
	private TaskContainer tasks;
	private ObjectFactory objectFactory;
	private final VariantComponentDependencies<?> dependencies;

	public ExecutableBinaryFactoryImpl(TaskContainer tasks, ObjectFactory objectFactory, VariantComponentDependencies<?> dependencies) {
		this.tasks = tasks;
		this.objectFactory = objectFactory;
		this.dependencies = dependencies;
	}

	@Override
	public ExecutableBinary create(NamingScheme names, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets) {
		val linkTask = tasks.register(names.getTaskName("link"), LinkExecutableTask.class);
		return objectFactory.newInstance(ExecutableBinaryInternal.class, names, objectSourceSets, targetMachine, linkTask, dependencies.getIncoming());
	}
}
