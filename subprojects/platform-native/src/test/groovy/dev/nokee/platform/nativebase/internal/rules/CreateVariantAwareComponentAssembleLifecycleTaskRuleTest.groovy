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
package dev.nokee.platform.nativebase.internal.rules

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.VariantAwareComponentInternal
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.utils.TaskUtils.configureGroup

@Subject(CreateVariantAwareComponentAssembleLifecycleTaskRule)
class CreateVariantAwareComponentAssembleLifecycleTaskRuleTest extends Specification implements TaskEntityFixture, ComponentEntityFixture {
	Project project = ProjectTestUtils.rootProject()
	def subject = new CreateVariantAwareComponentAssembleLifecycleTaskRule(taskRegistry)

	VariantAwareComponentInternal<?> aComponent(ComponentIdentifier<?> identifier) {
		return Stub(VariantAwareComponentInternal) {
			getIdentifier() >> identifier
		}
	}

	@Unroll
	def "creates the assemble task if absent"(component) {
		given:
		discovered(component)

		when:
		subject.execute(aComponent(component))

		then:
		def assembleTask = taskRepository.get(aTaskOfComponent('assemble', component))
		assembleTask.group == 'build'

		where:
		component << [mainComponentIdentifier(), aComponentIdentifier('test'), aComponentIdentifier('integTest')]
	}

	@Unroll
	def "does not configure the assemble task group if already present"(component) {
		given:
		discovered(component)
		def assembleTask = taskRegistry.register(aTaskOfComponent('assemble', component), configureGroup('some group')).get()

		when:
		subject.execute(aComponent(component))

		then:
		assembleTask.group == 'some group'

		where:
		component << [mainComponentIdentifier(), aComponentIdentifier('test')]
	}

	@Unroll
	def "configures assemble task with a dependency on the buildable variant or warns"(component) {
		given:
		discovered(component)

		when:
		subject.execute(aComponent(component))

		then:
		def assembleTask = taskRepository.get(aTaskOfComponent('assemble', component))
		assembleTask.dependsOn.size() == 1 // We assume the dependency is buildable variant or warning logger

		where:
		component << [mainComponentIdentifier(), aComponentIdentifier('test')]
	}
}
