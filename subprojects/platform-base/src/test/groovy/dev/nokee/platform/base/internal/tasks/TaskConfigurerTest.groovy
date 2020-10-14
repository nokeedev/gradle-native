package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.AbstractDomainObjectConfigurerTest
import dev.nokee.model.internal.DomainObjectConfigurer
import org.gradle.api.Task
import spock.lang.Subject

@Subject(TaskConfigurer)
class TaskConfigurerTest extends AbstractDomainObjectConfigurerTest<Task> implements TaskFixture {
	@Override
	protected DomainObjectConfigurer<Task> newSubject() {
		return newEntityConfigurer()
	}
}
