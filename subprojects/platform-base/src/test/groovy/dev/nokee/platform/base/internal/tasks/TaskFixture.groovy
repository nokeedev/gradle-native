/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.base.internal.tasks

import dev.nokee.internal.testing.util.ProjectTestUtils
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Task

import java.util.function.Supplier

trait TaskFixture {
	def project = ProjectTestUtils.rootProject()

    RealizableDomainObjectRepository<Task> newEntityRepository() {
		def realizer = new RealizableDomainObjectRealizerImpl(eventPublisher)
		return new TaskRepository(eventPublisher, realizer, providerFactory)
	}

    DomainObjectConfigurer<Task> newEntityConfigurer() {
		return new TaskConfigurer(eventPublisher, project.getTasks())
	}

    KnownDomainObjectFactory<Task> newEntityFactory() {
		throw new UnsupportedOperationException()
	}

    DomainObjectViewFactory<Task> newEntityViewFactory() {
		throw new UnsupportedOperationException()
	}

	DomainObjectProviderFactory<Task> newEntityProviderFactory() {
		throw new UnsupportedOperationException()
	}

	TaskRegistry newTaskRegistry() {
		return new TaskRegistryImpl(ProjectIdentifier.of(project), eventPublisher, project.getTasks())
	}

	Class<Task> getEntityType() {
		return Task
	}

	Class<? extends Task> getEntityImplementationType() {
		return TaskImpl
	}

	def <S extends Task> TaskIdentifier<S> entityIdentifier(Class<S> type, DomainObjectIdentifier owner) {
		return TaskIdentifier.of(TaskName.of('a' + RandomStringUtils.randomAlphanumeric(12)), type, (ComponentIdentifier)owner)
	}

	def <S extends Task> TypeAwareDomainObjectIdentifier<S> entityDiscovered(TypeAwareDomainObjectIdentifier<S> identifier) {
		project.getTasks().register(identifier.taskName, identifier.type)
		return super.entityDiscovered(identifier)
	}

	def <S extends Task> List entity(TypeAwareDomainObjectIdentifier<S> identifier) {
		return [identifier, { project.getTasks().getByName(identifier.taskName) } as Supplier<S>]
	}

    DomainObjectIdentifier ownerIdentifier(String name) {
		return ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of('root'))
	}

	Class<? extends Task> getMyEntityType() {
		return MyTask
	}

	static class MyTask extends DefaultTask {}

	Class<? extends Task> getMyEntityChildType() {
		return MyTaskChild
	}

	static class MyTaskChild extends MyTask {}

	static class TaskImpl extends DefaultTask {}
}
