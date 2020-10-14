package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepositoryTest
import dev.nokee.model.internal.RealizableDomainObjectRealizer
import dev.nokee.model.internal.RealizableDomainObjectRepository
import org.gradle.api.Task

class TaskRepositoryTest extends AbstractRealizableDomainObjectRepositoryTest<Task> implements TaskFixture {
	@Override
	protected RealizableDomainObjectRepository<Task> newSubject(RealizableDomainObjectRealizer realizer) {
		return new TaskRepository(eventPublisher, realizer, providerFactory)
	}
}
