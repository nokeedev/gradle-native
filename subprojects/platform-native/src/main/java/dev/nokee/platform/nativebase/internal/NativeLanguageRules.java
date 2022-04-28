/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.FunctionalSourceSet;
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
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
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
	private final DomainObjectIdentifier ownerIdentifier;

	public NativeLanguageRules(TaskRegistry taskRegistry, ObjectFactory objects, DomainObjectIdentifier ownerIdentifier) {
		this.taskRegistry = taskRegistry;
		this.objects = objects;
		this.ownerIdentifier = ownerIdentifier;
	}

	public DomainObjectSet<ObjectSourceSet> apply(FunctionalSourceSet sourceSets) {
		val objectSourceSets = objects.domainObjectSet(ObjectSourceSet.class);
		sourceSets.whenElementKnown(CSourceSet.class, it -> createNativeCompileTask(CCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(CppSourceSet.class, it -> createNativeCompileTask(CppCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(ObjectiveCSourceSet.class, it -> createNativeCompileTask(ObjectiveCCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(ObjectiveCppSourceSet.class, it -> createNativeCompileTask(ObjectiveCppCompileTask.class).andThen(this::newObjectSourceSetFromNativeCompileTask).andThen(objectSourceSets::add).apply(it));
		sourceSets.whenElementKnown(SwiftSourceSet.class, it -> ((Function<KnownDomainObject<? extends LanguageSourceSet>, TaskProvider<SwiftCompileTask>>)this::createSwiftCompileTask).andThen(this::newObjectSourceSetFromSwiftCompileTask).andThen(objectSourceSets::add).apply(it));
		return objectSourceSets;
	}

	private <T extends AbstractNativeCompileTask> Function<KnownDomainObject<? extends LanguageSourceSet>, TaskProvider<T>> createNativeCompileTask(Class<T> type) {
		return knownSourceSet -> {
			return taskRegistry.register(TaskIdentifier.of(TaskName.of("compile", DomainObjectIdentifierUtils.toPath(knownSourceSet.getIdentifier()).getName()), type, ownerIdentifier),task -> {
				task.getSource().from(knownSourceSet.map(LanguageSourceSet::getAsFileTree));
			});
		};
	}

	private TaskProvider<SwiftCompileTask> createSwiftCompileTask(KnownDomainObject<? extends LanguageSourceSet> sourceSet) {
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
