package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import javax.inject.Inject;
import java.util.function.Function;

public abstract class NativeLanguageRules {
	@Getter private final NamingScheme names;

	@Inject
	public NativeLanguageRules(NamingScheme names) {
		this.names = names;
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	public DomainObjectSet<GeneratedSourceSet> apply(DomainObjectSet<SourceSet> sourceSets) {
		DomainObjectSet<GeneratedSourceSet> objectSourceSets = getObjects().domainObjectSet(GeneratedSourceSet.class);
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

	private <T extends AbstractNativeCompileTask> Function<SourceSet, TaskProvider<T>> createNativeCompileTask(String languageName, Class<T> type) {
		return sourceSet -> {
			return getTasks().register(getNames().getTaskName("compile", languageName), type, task -> {
				task.getSource().from(sourceSet.getAsFileTree());
			});
		};
	}

	private TaskProvider<SwiftCompileTask> createSwiftCompileTask(SourceSet sourceSet) {
		return getTasks().register(getNames().getTaskName("compile", "Swift"), SwiftCompileTask.class, task -> {
			task.getSource().from(sourceSet.getAsFileTree());
		});
	}

	private <T extends AbstractNativeCompileTask> GeneratedSourceSet newObjectSourceSetFromNativeCompileTask(TaskProvider<T> task) {
		return getObjects().newInstance(GeneratedSourceSet.class, new UTTypeObjectCode(), task.flatMap(AbstractNativeCompileTask::getObjectFileDir), task);
	}

	private GeneratedSourceSet newObjectSourceSetFromSwiftCompileTask(TaskProvider<SwiftCompileTask> task) {
		return getObjects().newInstance(GeneratedSourceSet.class, new UTTypeObjectCode(), task.flatMap(SwiftCompileTask::getObjectFileDir), task);
	}
}
