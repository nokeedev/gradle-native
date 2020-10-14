package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.AbstractDomainObjectViewTest
import org.gradle.api.Task
import spock.lang.Subject

@Subject(TaskViewImpl)
class TaskViewTest extends AbstractDomainObjectViewTest<Task> implements TaskFixture {
}
