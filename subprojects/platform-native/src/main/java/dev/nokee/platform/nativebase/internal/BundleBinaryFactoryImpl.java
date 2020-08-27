package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.tasks.ComponentTasksInternal;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

public class BundleBinaryFactoryImpl implements BundleBinaryFactory {
	private final TaskContainer tasks;
	private final ObjectFactory objectFactory;
	private final VariantComponentDependencies<?> dependencies;

	public BundleBinaryFactoryImpl(TaskContainer tasks, ObjectFactory objectFactory, VariantComponentDependencies<?> dependencies) {
		this.tasks = tasks;
		this.objectFactory = objectFactory;
		this.dependencies = dependencies;
	}

	@Override
	public BundleBinary create(NamingScheme names, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets) {
		val linkTask = tasks.register(names.getTaskName("link"), LinkBundleTask.class);
		return objectFactory.newInstance(BundleBinaryInternal.class, names, targetMachine, objectSourceSets, linkTask, dependencies.getIncoming());
	}
}
