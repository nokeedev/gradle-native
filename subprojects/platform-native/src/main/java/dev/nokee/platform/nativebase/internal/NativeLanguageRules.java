package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.internal.ObjectSourceSetInternal;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import javax.inject.Inject;
import java.util.function.Function;

public class NativeLanguageRules {
	@Getter private final NamingScheme names;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public NativeLanguageRules(NamingScheme names, TaskContainer tasks, ObjectFactory objects) {
		this.names = names;
		this.tasks = tasks;
		this.objects = objects;
	}

	public DomainObjectSet<ObjectSourceSetInternal> apply(DomainObjectSet<LanguageSourceSet> sourceSets) {
		DomainObjectSet<ObjectSourceSetInternal> objectSourceSets = getObjects().domainObjectSet(ObjectSourceSetInternal.class);
		sourceSets.withType(CSourceSet.class).stream()
			.map(createNativeCompileTask("C", CCompileTask.class))
			.map(this::newObjectSourceSetFromNativeCompileTask)
			.forEach(objectSourceSets::add);
		sourceSets.withType(CppSourceSet.class).stream()
			.map(createNativeCompileTask("Cpp", CppCompileTask.class))
			.map(this::newObjectSourceSetFromNativeCompileTask)
			.forEach(objectSourceSets::add);
		sourceSets.withType(ObjectiveCSourceSet.class).stream()
			.map(createNativeCompileTask("ObjectiveC", ObjectiveCCompileTask.class))
			.map(this::newObjectSourceSetFromNativeCompileTask)
			.forEach(objectSourceSets::add);
		sourceSets.withType(ObjectiveCppSourceSet.class).stream()
			.map(createNativeCompileTask("ObjectiveCpp", ObjectiveCppCompileTask.class))
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
			return getTasks().register(getNames().getTaskName("compile", languageName), type, task -> {
				task.getSource().from(sourceSet.getAsFileTree());
			});
		};
	}

	private TaskProvider<SwiftCompileTask> createSwiftCompileTask(LanguageSourceSet sourceSet) {
		return getTasks().register(getNames().getTaskName("compile", "Swift"), SwiftCompileTask.class, task -> {
			task.getSource().from(sourceSet.getAsFileTree());
		});
	}

	private <T extends AbstractNativeCompileTask> ObjectSourceSetInternal newObjectSourceSetFromNativeCompileTask(TaskProvider<T> task) {
		return new ObjectSourceSetInternal(task.getName(), UTTypeObjectCode.INSTANCE, task.flatMap(AbstractNativeCompileTask::getObjectFileDir), (TaskProvider<? extends SourceCompile>)task);
	}

	private ObjectSourceSetInternal newObjectSourceSetFromSwiftCompileTask(TaskProvider<SwiftCompileTask> task) {
		return new ObjectSourceSetInternal(task.getName(), UTTypeObjectCode.INSTANCE, task.flatMap(SwiftCompileTask::getObjectFileDir), task);
	}
}
