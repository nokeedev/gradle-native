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
package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.AbstractDomainObjectViewTest
import org.gradle.api.Buildable
import org.gradle.api.Task
import spock.lang.Subject

@Subject(TaskViewImpl)
class TaskViewTest extends AbstractDomainObjectViewTest<Task> implements TaskFixture {
	def "is buildable"() {
		expect:
		newSubject() instanceof Buildable
	}

	def "can depends on each tasks inside task view"() {
		given:
		def subject = newSubject()
		def registry = newTaskRegistry()

		and:
		def entityProvider1 = registry.register(entityIdentifier(ownerIdentifier))
		def entityProvider2 = registry.register(entityIdentifier(ownerIdentifier))
		def entityProvider3 = registry.register(entityIdentifier(ownerIdentifier))

		expect:
		subject.buildDependencies.getDependencies(null) == [entityProvider1.get(), entityProvider2.get(), entityProvider3.get()] as Set
	}
}
