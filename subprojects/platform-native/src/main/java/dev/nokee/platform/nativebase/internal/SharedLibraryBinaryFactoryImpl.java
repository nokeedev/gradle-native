package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public class SharedLibraryBinaryFactoryImpl implements SharedLibraryBinaryFactory {
	private final TaskContainer tasks;
	private final ObjectFactory objectFactory;
	private final VariantComponentDependencies<?> dependencies;

	@Inject
	public SharedLibraryBinaryFactoryImpl(TaskContainer tasks, ObjectFactory objectFactory, VariantComponentDependencies<?> dependencies) {
		this.tasks = tasks;
		this.objectFactory = objectFactory;
		this.dependencies = dependencies;
	}

	@Override
	public SharedLibraryBinary create(NamingScheme names, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets) {
		val linkTask = tasks.register(names.getTaskName("link"), LinkSharedLibraryTask.class);

		return objectFactory.newInstance(SharedLibraryBinaryInternal.class, names, objectFactory.domainObjectSet(LanguageSourceSetInternal.class), targetMachine, objectSourceSets, linkTask, dependencies.getIncoming());
	}
}
