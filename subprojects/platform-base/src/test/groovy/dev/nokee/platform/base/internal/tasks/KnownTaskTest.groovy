package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.AbstractKnownDomainObjectTest
import org.gradle.api.Task
import spock.lang.Subject

@Subject(KnownTask)
class KnownTaskTest extends AbstractKnownDomainObjectTest<Task> implements TaskFixture {
}
