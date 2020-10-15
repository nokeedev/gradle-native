package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.LanguageSourceSet;
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

	public DomainObjectSet<ObjectSourceSet> apply(DomainObjectSet<LanguageSourceSet> sourceSets) {
		val objectSourceSets = getObjects().domainObjectSet(ObjectSourceSet.class);
		sourceSets.withType(CSourceSet.class).stream()
			.map(createNativeCompileTask("c", CCompileTask.class))
			.map(this::newObjectSourceSetFromNativeCompileTask)
			.forEach(objectSourceSets::add);
		sourceSets.withType(CppSourceSet.class).stream()
			.map(createNativeCompileTask("cpp", CppCompileTask.class))
			.map(this::newObjectSourceSetFromNativeCompileTask)
			.forEach(objectSourceSets::add);
		sourceSets.withType(ObjectiveCSourceSet.class).stream()
			.map(createNativeCompileTask("objectiveC", ObjectiveCCompileTask.class))
			.map(this::newObjectSourceSetFromNativeCompileTask)
			.forEach(objectSourceSets::add);
		sourceSets.withType(ObjectiveCppSourceSet.class).stream()
			.map(createNativeCompileTask("objectiveCpp", ObjectiveCppCompileTask.class))
			.map(this::newObjectSourceSetFromNativeCompileTask)
			.forEach(objectSourceSets::add);
		sourceSets.withType(SwiftSourceSet.class).stream()
			.map(this::createSwiftCompileTask)
			.map(this::newObjectSourceSetFromSwiftCompileTask)
			.forEach(objectSourceSets::add);
		return objectSourceSets;
	}

	private <T extends AbstractNativeCompileTask> Function<LanguageSourceSet, TaskProvider<T>> createNativeCompileTask(String languageName, Class<T> type) {
		return sourceSet -> {
			return taskRegistry.register(TaskIdentifier.of(TaskName.of("compile", languageName), type, ownerIdentifier), task -> {
				task.getSource().from(sourceSet.getAsFileTree());
			});
		};
	}

	private TaskProvider<SwiftCompileTask> createSwiftCompileTask(LanguageSourceSet sourceSet) {
		return taskRegistry.register(TaskIdentifier.of(TaskName.of("compile", "swift"), SwiftCompileTask.class, ownerIdentifier), task -> {
			task.getSource().from(sourceSet.getAsFileTree());
		});
	}

	private <T extends AbstractNativeCompileTask> ObjectSourceSet newObjectSourceSetFromNativeCompileTask(TaskProvider<T> task) {
		return new ObjectSourceSet(task.getName(), task.flatMap(AbstractNativeCompileTask::getObjectFileDir), task, objects);
	}

	private ObjectSourceSet newObjectSourceSetFromSwiftCompileTask(TaskProvider<SwiftCompileTask> task) {
		return new ObjectSourceSet(task.getName(), task.flatMap(SwiftCompileTask::getObjectFileDir), task, objects);
	}
}
