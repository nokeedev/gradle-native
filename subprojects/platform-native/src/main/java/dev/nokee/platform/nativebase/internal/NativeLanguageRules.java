package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.KnownLanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetViewInternal;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.internal.ObjectSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import java.util.function.Function;

public class NativeLanguageRules {
	private final TaskRegistry taskRegistry;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	private final DomainObjectIdentifierInternal ownerIdentifier;

	public NativeLanguageRules(TaskRegistry taskRegistry, ObjectFactory objects, DomainObjectIdentifierInternal ownerIdentifier) {
		this.taskRegistry = taskRegistry;
		this.objects = objects;
		this.ownerIdentifier = ownerIdentifier;
	}

	public DomainObjectSet<ObjectSourceSet> apply(LanguageSourceSetViewInternal<LanguageSourceSet> sourceSets) {
		val objectSourceSets = objects.domainObjectSet(ObjectSourceSet.class);
		sourceSets.whenElementKnown(CSourceSet.class, it -> createNativeCompileTask(CCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(CppSourceSet.class, it -> createNativeCompileTask(CppCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(ObjectiveCSourceSet.class, it -> createNativeCompileTask(ObjectiveCCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(ObjectiveCppSourceSet.class, it -> createNativeCompileTask(ObjectiveCppCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(SwiftSourceSet.class, it -> ((Function<KnownLanguageSourceSet<?>, TaskProvider<SwiftCompileTask>>)this::createSwiftCompileTask).andThen(this::newObjectSourceSetFromSwiftCompileTask).andThen(objectSourceSets::add).apply(it));
		return objectSourceSets;
	}

	private <T extends AbstractNativeCompileTask> Function<KnownLanguageSourceSet<?>, TaskProvider<T>> createNativeCompileTask(Class<T> type) {
		return knownSourceSet -> {
			return taskRegistry.register(TaskIdentifier.of(TaskName.of("compile", knownSourceSet.getIdentifier().getName().get()), type, ownerIdentifier), task -> {
				task.getSource().from(knownSourceSet.map(LanguageSourceSet::getAsFileTree));
			});
		};
	}

	private TaskProvider<SwiftCompileTask> createSwiftCompileTask(KnownLanguageSourceSet<?> sourceSet) {
		return taskRegistry.register(TaskIdentifier.of(TaskName.of("compile", "swift"), SwiftCompileTask.class, ownerIdentifier), task -> {
			task.getSource().from(sourceSet.map(LanguageSourceSet::getAsFileTree));
		});
	}

	private <T extends AbstractNativeCompileTask> ObjectSourceSet newObjectSourceSetFromNativeCompileTask(TaskProvider<T> task) {
		return new ObjectSourceSet(task.getName(), task.flatMap(AbstractNativeCompileTask::getObjectFileDir), task, objects);
	}

	private ObjectSourceSet newObjectSourceSetFromSwiftCompileTask(TaskProvider<SwiftCompileTask> task) {
		return new ObjectSourceSet(task.getName(), task.flatMap(SwiftCompileTask::getObjectFileDir), task, objects);
	}
}
