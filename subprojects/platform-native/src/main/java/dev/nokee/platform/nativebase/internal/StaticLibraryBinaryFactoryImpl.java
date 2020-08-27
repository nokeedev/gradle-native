package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.tasks.ComponentTasksInternal;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class StaticLibraryBinaryFactoryImpl implements StaticLibraryBinaryFactory {
	private final TaskContainer tasks;
	private final ObjectFactory objectFactory;
	private final VariantComponentDependencies<?> dependencies;

	@Inject
	public StaticLibraryBinaryFactoryImpl(TaskContainer tasks, ObjectFactory objectFactory, VariantComponentDependencies<?> dependencies) {
		this.tasks = tasks;
		this.objectFactory = objectFactory;
		this.dependencies = dependencies;
	}

	@Override
	public StaticLibraryBinary create(NamingScheme names, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets) {
		val createTask = tasks.register(names.getTaskName("create"), CreateStaticLibraryTask.class);
		return objectFactory.newInstance(StaticLibraryBinaryInternal.class, names, objectSourceSets, targetMachine, createTask, dependencies.getIncoming());
	}
}
